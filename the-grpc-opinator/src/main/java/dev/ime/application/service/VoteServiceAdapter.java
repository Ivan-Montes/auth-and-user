package dev.ime.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.ime.application.dto.VoteDto;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.common.mapper.VoteMapper;
import dev.ime.domain.model.Vote;
import dev.ime.domain.port.inbound.AuthorizationServicePort;
import dev.ime.domain.port.inbound.GenericServicePort;
import dev.ime.domain.port.outbound.GenericRepositoryPort;
import dev.ime.domain.port.outbound.PublisherPort;

@Service
public class VoteServiceAdapter implements GenericServicePort<VoteDto> {

	private final GenericRepositoryPort<Vote> voteRepositoryAdapter;
	private final VoteMapper voteMapper;
	private final EventMapper eventMapper;
	private final AuthorizationServicePort authorizationServiceAdapter;
	private final PublisherPort publisherAdapter;
	private static final Logger logger = LoggerFactory.getLogger(VoteServiceAdapter.class);

	public VoteServiceAdapter(GenericRepositoryPort<Vote> voteRepositoryAdapter, VoteMapper voteMapper,
			EventMapper eventMapper, AuthorizationServicePort authorizationServiceAdapter,
			PublisherPort publisherAdapter) {
		super();
		this.voteRepositoryAdapter = voteRepositoryAdapter;
		this.voteMapper = voteMapper;
		this.eventMapper = eventMapper;
		this.authorizationServiceAdapter = authorizationServiceAdapter;
		this.publisherAdapter = publisherAdapter;
	}

	@Override
	public List<VoteDto> findAll() {

		return voteMapper.fromListDomainToListDto(voteRepositoryAdapter.findAll());
	}

	@Override
	public List<VoteDto> findAll(Pageable pageable) {

		return voteMapper.fromListDomainToListDto(voteRepositoryAdapter.findAll(pageable));
	}

	@Override
	public Optional<VoteDto> findById(Long id) {

		return voteRepositoryAdapter.findById(id)
				.map(voteMapper::fromDomainToDto)
		        .map(item -> {
		            logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.VOT_CAT, item);
		            return item;
		        });
	}

	@Override
	public Optional<VoteDto> save(VoteDto dto) {
			
		return  Optional.of(dto)
				.map(voteMapper::fromDtoToDomain)
				.map( dom -> {
					dom.setEmail(authorizationServiceAdapter.getJwtTokenEmail());
					return dom;
				})
				.flatMap(voteRepositoryAdapter::save)
				.map(voteMapper::fromDomainToDto)
				.map( item -> {
					publisherAdapter.publishEvent(eventMapper.createEvent(GlobalConstants.VOT_CAT, GlobalConstants.VOT_CREATED, item));
					return item;
				})
		        .map(item -> {
		            logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.VOT_CREATED, item);
		            return item;
		        });
	}

	@Override
	public Optional<VoteDto> update(VoteDto dto) {

		String mailDb = getOwnerEmail(dto.voteId());
		authorizationServiceAdapter.checkJwtTokenOwner(mailDb);
		
		return  Optional.of(dto)
				.map(voteMapper::fromDtoToDomain)
				.flatMap(voteRepositoryAdapter::update)
				.map(voteMapper::fromDomainToDto)
				.map( item -> {
					publisherAdapter.publishEvent(eventMapper.createEvent(GlobalConstants.VOT_CAT, GlobalConstants.VOT_UPDATED, item));
					return item;
				})
		        .map(item -> {
		            logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.VOT_UPDATED, item);
		            return item;
		        });
	}

	private String getOwnerEmail(Long id) {
		
		return voteRepositoryAdapter.findById(id)
				.map(Vote::getEmail)
				.orElseThrow(() -> new ResourceNotFoundException(
				Map.of(GlobalConstants.VOT_ID, String.valueOf(id))));
	}

	@Override
	public boolean deleteById(Long id) {
		
		return Optional.of(id)
				.map(this::getOwnerEmail)
				.map( mailDb -> {
					authorizationServiceAdapter.checkJwtTokenOwner(mailDb);
					return id;
				})
				.map(voteRepositoryAdapter::deleteById)
				.map( b -> processDelete(b,id))
				.orElseThrow(() -> new ResourceNotFoundException(
						Map.of(GlobalConstants.VOT_ID, String.valueOf(id))));
	}

	private boolean processDelete(boolean result, Long id) {

		Optional.ofNullable(id)
		.map( i -> new VoteDto(id,"",-1L,false))
		.map(item -> eventMapper.createEvent(GlobalConstants.VOT_CAT, GlobalConstants.VOT_DELETED, item))
		.ifPresent(publisherAdapter::publishEvent);

		return result;
	}

}
