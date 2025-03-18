package dev.ime.common.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.ime.application.dto.ReviewDto;
import dev.ime.common.config.GlobalConstants;
import dev.ime.domain.model.Category;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.Product;
import dev.ime.domain.model.Review;
import dev.ime.domain.model.Vote;
import dev.ime.infrastructure.entity.ProductJpaEntity;
import dev.ime.infrastructure.entity.ReviewJpaEntity;
import dev.ime.infrastructure.entity.VoteJpaEntity;
import dev.proto.CreateReviewRequest;
import dev.proto.ReviewProto;
import dev.proto.UpdateReviewRequest;

@Component
public class ReviewMapper {

	private final ObjectMapper objectMapper;
	
	public ReviewMapper(ObjectMapper objectMapper) {
		super();
		this.objectMapper = objectMapper;
	}

	public ReviewDto fromCreateToDto(CreateReviewRequest request) {

		return new ReviewDto(null, null, request.getProductId(), request.getReviewText(),
				request.getRating());

	}

	public ReviewDto fromUpdateToDto(UpdateReviewRequest request) {

		return new ReviewDto(request.getReviewId(), null, -1L, request.getReviewText(), request.getRating());

	}

	public Review fromDtoToDomain(ReviewDto dto) {

		Product product = new Product();
		product.setProductId(dto.productId());
		return new Review(
				dto.reviewId(), 
				dto.email(), 
				product, 
				dto.reviewText(), 
				dto.rating(), 
				new HashSet<>());
	}

	public ReviewJpaEntity fromDomainToJpa(Review dom, ProductJpaEntity productJpaEntity) {
		
		ReviewJpaEntity reviewJpaEntity = new ReviewJpaEntity(
				dom.getReviewId(),
				dom.getEmail(),
				productJpaEntity,
				dom.getReviewText(),
				dom.getRating(),
				new HashSet<>()
				);
		productJpaEntity.getReviews().add(reviewJpaEntity);
		
		return reviewJpaEntity;
	}

	public Review fromJpaToDomain(ReviewJpaEntity entity) {
		
		Product product = fromProductJpaToProductDomain(entity.getProduct());
		
		Review review = new Review(
				entity.getReviewId(),
				entity.getEmail(),
				product,
				entity.getReviewText(),
				entity.getRating(),
				null
				);	
		Set<Vote> votes = fromSetVoteJpaToSetVoteDom(entity.getVotes(), review);
		review.setVotes(votes);
		
		return review;
	}

	public Product fromProductJpaToProductDomain(ProductJpaEntity entity) {		
		
		Category category = new Category();
		category.setCategoryId(entity.getCategory().getCategoryId());

		Product product = new Product(
				entity.getProductId(),
				entity.getProductName(),
				entity.getProductDescription(),
				category,
				new HashSet<>()
				);
		category.addProduct(product);
		
		return product;
	}

	private Vote fromVoteJpaToVoteDom(VoteJpaEntity voteJpaEntity, Review review) {

		return new Vote(
				voteJpaEntity.getVoteId(),
				voteJpaEntity.getEmail(),
				review,
				voteJpaEntity.isUseful()
				);
	}
	
	private Set<Vote> fromSetVoteJpaToSetVoteDom(Set<VoteJpaEntity> votesJpa, Review review) {

		if ( votesJpa == null ) {
			return new HashSet<>();
		}

		return votesJpa.stream()
				.map(  v -> fromVoteJpaToVoteDom(v, review))
				.collect(Collectors.toSet());	
	}

	public List<Review> fromListJpaToListDomain(List<ReviewJpaEntity> listJpa) {

		if (listJpa == null) {
			return new ArrayList<>();
		}

		return listJpa.stream().map(this::fromJpaToDomain).toList();
	}
	
	public ReviewProto fromDtoToProto(ReviewDto dto) {

		return ReviewProto.newBuilder().setReviewId(dto.reviewId()).setEmail(dto.email()).setProductId(dto.productId())
				.setReviewText(dto.reviewText()).setRating(dto.rating()).build();

	}
	
	public ReviewDto fromDomainToDto(Review dom) {
		
		return new ReviewDto(
				dom.getReviewId(),
				dom.getEmail(),
				dom.getProduct().getProductId(),
				dom.getReviewText(),
				dom.getRating()
				);
	}
	
	public List<ReviewDto> fromListDomainToListDto(List<Review> list) {

		if (list == null) {
			return new ArrayList<>();
		}

		return list.stream().map(this::fromDomainToDto).toList();
	}

	public List<ReviewProto> fromListDtoToListProto(List<ReviewDto> listDto) {

		if (listDto == null) {
			return new ArrayList<>();
		}

		return listDto.stream().map(this::fromDtoToProto).toList();

	}

	public Event fromDtoToEvent(String eventType, ReviewDto dto) {		
		
		return new Event(
				GlobalConstants.REV_CAT,
				eventType,
				createEventData(dto)
				);		
	}
	
	private Map<String, Object> createEventData(ReviewDto dto) {

		return objectMapper.convertValue(dto, new TypeReference<Map<String, Object>>() {
		});

	}
	
}
