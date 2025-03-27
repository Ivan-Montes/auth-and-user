package dev.ime.application.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import dev.ime.application.dto.ReviewDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.common.mapper.ReviewMapper;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.Product;
import dev.ime.domain.model.Review;
import dev.ime.domain.port.inbound.AuthorizationServicePort;
import dev.ime.domain.port.outbound.GenericRepositoryPort;
import dev.ime.domain.port.outbound.PublisherPort;

@ExtendWith(MockitoExtension.class)
class ReviewServiceAdapterTest {

	@Mock
	private GenericRepositoryPort<Review> reviewRepositoryAdapter;
	@Mock
	private ReviewMapper reviewMapper;
	@Mock
	private EventMapper eventMapper;
	@Mock
	private AuthorizationServicePort authorizationServiceAdapter;
	@Mock
	private PublisherPort publisherAdapter;

	@InjectMocks
	private ReviewServiceAdapter reviewServiceAdapter;

	private ReviewDto reviewDto;
	private Review review;
	private Event event;	

	private final Long reviewId = 1L;
	private final String email = "email@email.tk";
	private final Long productId = 1L;
	private final String reviewText = "Excellent";
	private final Integer rating = 5;

	private UUID eventId;
	private final String eventCategory = GlobalConstants.REV_CAT;
	private final String eventType = GlobalConstants.REV_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private final Map<String, Object> eventData = new HashMap<>();

	private final Pageable pageable = PageRequest.of(0, 10, Sort.by(GlobalConstants.REV_ID).ascending());
		
	@BeforeEach
	private void setUp(){
		
		reviewDto = new ReviewDto(reviewId, email, productId, reviewText, rating);

		review = new Review();
		review.setReviewId(reviewId);
		review.setEmail(email);
		review.setReviewText(reviewText);
		review.setRating(rating);
		review.setProduct(new Product());
		
		eventId = UUID.randomUUID();
		
		event = new Event(
				eventId,
				eventCategory,
				eventType,
				eventTimestamp,
				eventData);			
	}

	@Test
	void findAll_shouldReturnList() {
		
		Mockito.when(reviewRepositoryAdapter.findAll()).thenReturn(List.of(review));
		Mockito.when(reviewMapper.fromListDomainToListDto(Mockito.anyList())).thenReturn(List.of(reviewDto));
		
		List<ReviewDto> list = reviewServiceAdapter.findAll();
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);		
	}

	@Test
	void findAll_withPageable_shouldReturnList() {
		
		Mockito.when(reviewRepositoryAdapter.findAll(Mockito.any(Pageable.class))).thenReturn(List.of(review));
		Mockito.when(reviewMapper.fromListDomainToListDto(Mockito.anyList())).thenReturn(List.of(reviewDto));
		
		List<ReviewDto> list = reviewServiceAdapter.findAll(pageable);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);		
	}

	@Test
	void findById_shouldReturnOptDto() {
		
		Mockito.when(reviewRepositoryAdapter.findById(Mockito.anyLong())).thenReturn(Optional.of(review));
		Mockito.when(reviewMapper.fromDomainToDto(Mockito.any(Review.class))).thenReturn(reviewDto);
		
		Optional<ReviewDto> optResult =  reviewServiceAdapter.findById(reviewId);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().reviewId()).isEqualTo(reviewId)
		);
	}

	@Test
	void save_shouldReturnOptDto() {
		
		Mockito.when(reviewMapper.fromDtoToDomain(Mockito.any(ReviewDto.class))).thenReturn(review);
		Mockito.when(authorizationServiceAdapter.getJwtTokenEmail()).thenReturn(email);
		Mockito.when(reviewRepositoryAdapter.save(Mockito.any(Review.class))).thenReturn(Optional.of(review));
		Mockito.when(reviewMapper.fromDomainToDto(Mockito.any(Review.class))).thenReturn(reviewDto);
		Mockito.when(eventMapper.createEvent(Mockito.anyString(),Mockito.anyString(),Mockito.any(ReviewDto.class))).thenReturn(event);
		Mockito.doNothing().when(publisherAdapter).publishEvent(Mockito.any(Event.class));

		Optional<ReviewDto> optResult =  reviewServiceAdapter.save(reviewDto);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().reviewId()).isEqualTo(reviewId)
		);
	}

	@Test
	void update_shouldReturnOptDto() {
		
		Mockito.when(reviewRepositoryAdapter.findById(Mockito.anyLong())).thenReturn(Optional.of(review));
		Mockito.doNothing().when(authorizationServiceAdapter).checkJwtTokenOwner(Mockito.anyString());
		Mockito.when(reviewMapper.fromDtoToDomain(Mockito.any(ReviewDto.class))).thenReturn(review);
		Mockito.when(reviewRepositoryAdapter.update(Mockito.any(Review.class))).thenReturn(Optional.of(review));
		Mockito.when(reviewMapper.fromDomainToDto(Mockito.any(Review.class))).thenReturn(reviewDto);
		Mockito.when(eventMapper.createEvent(Mockito.anyString(),Mockito.anyString(),Mockito.any(ReviewDto.class))).thenReturn(event);
		Mockito.doNothing().when(publisherAdapter).publishEvent(Mockito.any(Event.class));

		Optional<ReviewDto> optResult =  reviewServiceAdapter.update(reviewDto);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().reviewId()).isEqualTo(reviewId)
		);
	}

	@Test
	void deleteById_shouldReturnTrue() {
		
		Mockito.when(reviewRepositoryAdapter.findById(Mockito.anyLong())).thenReturn(Optional.of(review));
		Mockito.doNothing().when(authorizationServiceAdapter).checkJwtTokenOwner(Mockito.anyString());

		Mockito.when(reviewRepositoryAdapter.deleteById(Mockito.anyLong())).thenReturn(true);
		Mockito.when(eventMapper.createEvent(Mockito.anyString(),Mockito.anyString(),Mockito.any(ReviewDto.class))).thenReturn(event);
		Mockito.doNothing().when(publisherAdapter).publishEvent(Mockito.any(Event.class));
		
		boolean result = reviewServiceAdapter.deleteById(reviewId);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(result).isTrue()
		);		
	}
	
}
