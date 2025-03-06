package dev.ime.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.ime.application.dto.RegisterRequestDto;
import dev.ime.application.dto.UserAppDto;
import dev.ime.application.dto.UserDto;
import dev.ime.application.exception.EmailUsedException;
import dev.ime.application.exception.GrpcClientCommunicationException;
import dev.ime.common.config.GlobalConstants;
import dev.ime.common.mapper.UserMapper;
import dev.ime.domain.model.Role;
import dev.ime.domain.model.User;
import dev.ime.domain.port.inbound.AuthGrpcServicePort;
import dev.ime.domain.port.outbound.PublisherPort;
import dev.ime.domain.port.outbound.UserRepository;

@Service
public class AuthGrpcServiceAdapter implements AuthGrpcServicePort<UserDto,RegisterRequestDto>{

	private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAppGrpcClient userAppGrpcClient;
    private final UserMapper userMapper;
	private final PublisherPort publisherAdapter;
	private static final Logger logger = LoggerFactory.getLogger(AuthGrpcServiceAdapter.class);

	public AuthGrpcServiceAdapter(UserRepository userRepository, PasswordEncoder passwordEncoder,
			UserAppGrpcClient userAppGrpcClient, UserMapper userMapper, PublisherPort publisherAdapter) {
		super();
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.userAppGrpcClient = userAppGrpcClient;
		this.userMapper = userMapper;
		this.publisherAdapter = publisherAdapter;
	}

	@Override
	public List<UserDto> findAll() {
		
		return userMapper.fromListDomainToListDto(userRepository.findAll());	 
	}

	@Override
	public List<UserDto> findAll(Pageable pageable) {
		
		return userMapper.fromListDomainToListDto(userRepository.findAll(pageable));
	}
	
	@Transactional("transactionManager")
	@Override
	public Optional<UserDto> register(RegisterRequestDto registerRequestDto) {
		
		checkEmailAvailability(registerRequestDto.email());	
		User user = createNewDomainUserObject(registerRequestDto);		
		User userSaved = userRepository.save(user);
		UserAppDto userAppDto = createUserAppDto(registerRequestDto,userSaved);		
		boolean result = userAppGrpcClient.createUser(userAppDto);
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.MSG_CONRESULT, result);

		if ( !result ) {
			throw new GrpcClientCommunicationException(Map.of(GlobalConstants.MSG_CONRESULT, String.valueOf(result)));
		}
		
		publisherAdapter.publishEvent(userMapper.fromDomToEvent(userSaved));
		
		return Optional.ofNullable(userSaved)
				.map(userMapper::fromDomainToDto);		
	}

	private User createNewDomainUserObject(RegisterRequestDto registerRequestDto) {
		
		return new User(
				null,
				registerRequestDto.email(),
				passwordEncoder.encode( registerRequestDto.password()),
				Role.USER
				);
	}
	
	private void checkEmailAvailability(String email) {
		
	    if (userRepository.findByEmail(email).isPresent()) {
	    	
	        throw new EmailUsedException(Map.of(GlobalConstants.USER_EMAIL, email));
	    
	    }
	}
	
	private UserAppDto createUserAppDto(RegisterRequestDto registerRequestDto, User userSaved) {		
		
		return new UserAppDto(
				null,
				userSaved.getEmail(),
				registerRequestDto.name(),
				registerRequestDto.lastname()				
				);		
	}

}
