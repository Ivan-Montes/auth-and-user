package dev.ime.api.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.proto.CreateReviewRequest;
import dev.proto.DeleteReviewRequest;
import dev.proto.GetReviewRequest;
import dev.proto.UpdateReviewRequest;

@ExtendWith(MockitoExtension.class)
class ReviewRequestValidatorTest {

	private ReviewRequestValidator dtoValidator;

	private final Long reviewId = 1L;
	private final Long productId = 1L;
	private final String reviewText = "Excellent";
	private final Integer rating = 5;

	@BeforeEach
	private void setUp(){
		
		dtoValidator = new ReviewRequestValidator();
	}

	@Test
	void validateCreateRequest_shouldValidateRight() {
		
		CreateReviewRequest request = CreateReviewRequest.newBuilder()
				.setProductId(productId)
				.setReviewText(reviewText)
				.setRating(rating)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateCreateRequest(request));

	}

	@Test
	void validateUpdateRequest_shouldValidateRight() {
		
		UpdateReviewRequest request = UpdateReviewRequest.newBuilder()
				.setReviewId(reviewId)
				.setReviewText(reviewText)
				.setRating(rating)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateUpdateRequest(request));

	}

	@Test
	void validateDeleteRequest_shouldValidateRight() {
		
		DeleteReviewRequest request = DeleteReviewRequest.newBuilder()
				.setReviewId(reviewId)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateDeleteRequest(request));

	}

	@Test
	void validateGetRequest_shouldValidateRight() {
		
		GetReviewRequest request = GetReviewRequest.newBuilder()
				.setReviewId(reviewId)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateGetRequest(request));

	}

}
