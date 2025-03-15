package dev.ime.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.ime.application.dto.UserAppDto;
import dev.ime.application.exception.CreateJpaEntityException;
import dev.ime.application.exception.EmailNotChageException;
import dev.ime.application.exception.JwtTokenEmailRestriction;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.utils.JwtUtil;
import dev.ime.common.config.GlobalConstants;
import dev.ime.common.mapper.UserAppMapper;
import dev.ime.domain.model.UserApp;
import dev.ime.domain.port.inbound.UserAppServicePort;
import dev.ime.domain.port.outbound.PublisherPort;
import dev.ime.domain.port.outbound.UserAppRepositoryPort;

@Service
public class UserAppServiceAdapter implements UserAppServicePort<UserAppDto>{

	private final UserAppRepositoryPort userAppRepository;
	private final UserAppMapper userAppMapper;
	private final JwtUtil jwtUtil;
	private final PublisherPort publisherAdapter;
	private static final Logger logger = LoggerFactory.getLogger(UserAppServiceAdapter.class);

	public UserAppServiceAdapter(UserAppRepositoryPort userAppRepository, UserAppMapper userAppMapper, JwtUtil jwtUtil,
			PublisherPort publisherAdapter) {
		super();
		this.userAppRepository = userAppRepository;
		this.userAppMapper = userAppMapper;
		this.jwtUtil = jwtUtil;
		this.publisherAdapter = publisherAdapter;
	}

	@Override
	public List<UserAppDto> findAll() {

		return userAppMapper.fromListDomainToListDto(userAppRepository.findAll());
	}

	@Override
	public List<UserAppDto> findAll(Pageable pageable) {
		
		return userAppMapper.fromListDomainToListDto(userAppRepository.findAll(pageable));
	}

	@Override
	public boolean create(UserAppDto userAppDto) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.CREATE_USERAPP, userAppDto);

		if (userAppRepository.countByEmail(userAppDto.email()) > 0) {
			logger.error(GlobalConstants.MSG_PATTERN_SEVERE, GlobalConstants.MSG_ENTITY_ALREADY, userAppDto);
			throw new CreateJpaEntityException(Map.of(GlobalConstants.OBJ_R, GlobalConstants.MSG_ENTITY_ALREADY,
					GlobalConstants.USERAPP_CAT, String.valueOf(userAppDto)));
		}

		UserApp userApp = userAppMapper.fromDtoToDomain(userAppDto);
		Optional<UserApp> optUserAppSaved = userAppRepository.save(userApp);		
		optUserAppSaved.ifPresent( userappsaved -> publisherAdapter.publishEvent(userAppMapper.fromDomToEvent(GlobalConstants.USERAPP_CREATED, userappsaved)));
		
		return optUserAppSaved.isPresent();
	}

	public Optional<UserAppDto> update(UserAppDto userAppDto) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.UPDATE_USERAPP, userAppDto);

		Optional<UserApp> optUserAppFoud = userAppRepository.findById(userAppDto.userAppId());

		checkUserAppExists(userAppDto, optUserAppFoud);
		checkEmailNotChange(userAppDto, optUserAppFoud);
		checkJwtTokenOwner(userAppDto);

		UserApp userApp = userAppMapper.fromDtoToDomain(userAppDto);

		return userAppRepository.save(userApp)
				.map( userUpdated -> {
					publisherAdapter.publishEvent(userAppMapper.fromDomToEvent(GlobalConstants.USERAPP_UPDATED, userUpdated));
					return userUpdated;
				})
				.map(userAppMapper::fromDomainToDto);
	}

	private void checkJwtTokenOwner(UserAppDto userAppDto) {

		String jwtTokenMail = jwtUtil.getSubFromJwt();
		String userEmail = userAppDto.email();

		if (!userEmail.equals(jwtTokenMail)) {
			logger.error(GlobalConstants.MSG_PATTERN_SEVERE, GlobalConstants.EX_JWTTOKENEMAILRESTRICTION_DESC,
					userAppDto);
			throw new JwtTokenEmailRestriction(Map.of(GlobalConstants.USERAPP_CAT, String.valueOf(userAppDto)));
		}
	}

	private void checkEmailNotChange(UserAppDto userAppDto, Optional<UserApp> optUserAppFoud) {
		if (optUserAppFoud.isEmpty() || !userAppDto.email().equals(optUserAppFoud.get().getEmail())) {
			logger.error(GlobalConstants.MSG_PATTERN_SEVERE, GlobalConstants.EX_EMAILNOTCHANGE_DESC, userAppDto);
			throw new EmailNotChageException(Map.of(GlobalConstants.USERAPP_CAT, String.valueOf(userAppDto)));
		}
	}

	private void checkUserAppExists(UserAppDto userAppDto, Optional<UserApp> optUserAppFoud) {
		if (optUserAppFoud.isEmpty()) {
			logger.error(GlobalConstants.MSG_PATTERN_SEVERE, GlobalConstants.EX_RESOURCENOTFOUND_DESC, userAppDto);
			throw new ResourceNotFoundException(Map.of(GlobalConstants.USERAPP_CAT, String.valueOf(userAppDto)));
		}
	}

}
