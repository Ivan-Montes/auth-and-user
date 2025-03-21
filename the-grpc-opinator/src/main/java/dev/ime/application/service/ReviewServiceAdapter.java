package dev.ime.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.ime.application.dto.ReviewDto;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.common.mapper.ReviewMapper;
import dev.ime.domain.model.Review;
import dev.ime.domain.port.inbound.AuthorizationServicePort;
import dev.ime.domain.port.inbound.GenericServicePort;
import dev.ime.domain.port.outbound.GenericRepositoryPort;
import dev.ime.domain.port.outbound.PublisherPort;

@Service
public class ReviewServiceAdapter implements GenericServicePort<ReviewDto> {

	private final GenericRepositoryPort<Review> reviewRepositoryAdapter;
	private final ReviewMapper reviewMapper;
	private final EventMapper eventMapper;
	private final AuthorizationServicePort authorizationServiceAdapter;
	private final PublisherPort publisherAdapter;
	private static final Logger logger = LoggerFactory.getLogger(ReviewServiceAdapter.class);

	public ReviewServiceAdapter(GenericRepositoryPort<Review> reviewRepositoryAdapter, ReviewMapper reviewMapper,
			EventMapper eventMapper, AuthorizationServicePort authorizationServiceAdapter,
			PublisherPort publisherAdapter) {
		super();
		this.reviewRepositoryAdapter = reviewRepositoryAdapter;
		this.reviewMapper = reviewMapper;
		this.eventMapper = eventMapper;
		this.authorizationServiceAdapter = authorizationServiceAdapter;
		this.publisherAdapter = publisherAdapter;
	}

	@Override
	public List<ReviewDto> findAll() {
		
		return reviewMapper.fromListDomainToListDto(reviewRepositoryAdapter.findAll());
	}

	@Override
	public List<ReviewDto> findAll(Pageable pageable) {
		
		return reviewMapper.fromListDomainToListDto(reviewRepositoryAdapter.findAll(pageable));
	}

	@Override
	public Optional<ReviewDto> findById(Long id) {

		return reviewRepositoryAdapter.findById(id)
				.map(reviewMapper::fromDomainToDto)
		        .map(item -> {
		            logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.REV_CAT, item);
		            return item;
		        });
	}

	@Override
	public Optional<ReviewDto> save(ReviewDto dto) {
		
		return  Optional.of(dto)
				.map(reviewMapper::fromDtoToDomain)
				.map( dom -> {
					dom.setEmail(authorizationServiceAdapter.getJwtTokenEmail());
					return dom;
				})
				.flatMap(reviewRepositoryAdapter::save)
				.map(reviewMapper::fromDomainToDto)
				.map( item -> {
					publisherAdapter.publishEvent(eventMapper.createEvent(GlobalConstants.REV_CAT, GlobalConstants.REV_CREATED, item));
					return item;
				})
		        .map(item -> {
		            logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.REV_CREATED, item);
		            return item;
		        });
	}

	@Override
	public Optional<ReviewDto> update(ReviewDto dto) {
		
		String mailDb = getOwnerEmail(dto.reviewId());
		authorizationServiceAdapter.checkJwtTokenOwner(mailDb);
		
		return  Optional.of(dto)
				.map(reviewMapper::fromDtoToDomain)
				.flatMap(reviewRepositoryAdapter::update)
				.map(reviewMapper::fromDomainToDto)
				.map( item -> {
					publisherAdapter.publishEvent(eventMapper.createEvent(GlobalConstants.REV_CAT, GlobalConstants.REV_UPDATED, item));
					return item;
				})
		        .map(item -> {
		            logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.REV_UPDATED, item);
		            return item;
		        });
	}
	
	private String getOwnerEmail(Long id) {
		
		return reviewRepositoryAdapter.findById(id)
				.map(Review::getEmail)
				.orElseThrow(() -> new ResourceNotFoundException(
				Map.of(GlobalConstants.REV_ID, String.valueOf(id))));
	}

	@Override
	public boolean deleteById(Long id) {

		return Optional.of(id)
				.map(this::getOwnerEmail)
				.map( mailDb -> {
					authorizationServiceAdapter.checkJwtTokenOwner(mailDb);
					return id;
				})
				.map(reviewRepositoryAdapter::deleteById)
				.map( b -> processDelete(b,id))
				.orElseThrow(() -> new ResourceNotFoundException(
						Map.of(GlobalConstants.REV_ID, String.valueOf(id))));
	}

	private boolean processDelete(boolean result, Long id) {

		Optional.ofNullable(id)
		.map( i -> new ReviewDto(id,null,-1L,"",-1))
		.map(item -> eventMapper.createEvent(GlobalConstants.REV_CAT, GlobalConstants.REV_DELETED, item))
		.ifPresent(publisherAdapter::publishEvent);

		return result;
	}

}
