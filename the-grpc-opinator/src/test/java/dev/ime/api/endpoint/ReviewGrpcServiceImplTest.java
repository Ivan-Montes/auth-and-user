package dev.ime.api.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.google.protobuf.Empty;

import dev.ime.api.validation.ReviewRequestValidator;
import dev.ime.application.dto.ReviewDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.ReviewMapper;
import dev.ime.domain.port.inbound.GenericServicePort;
import dev.proto.CreateReviewRequest;
import dev.proto.DeleteReviewRequest;
import dev.proto.DeleteReviewResponse;
import dev.proto.GetReviewRequest;
import dev.proto.ListReviewsResponse;
import dev.proto.PaginationRequest;
import dev.proto.ReviewProto;
import dev.proto.UpdateReviewRequest;
import io.grpc.internal.testing.StreamRecorder;

@ExtendWith(MockitoExtension.class)
class ReviewGrpcServiceImplTest {

	@Mock
	private GenericServicePort<ReviewDto> reviewServiceAdapter;
	@Mock
	private ReviewMapper reviewMapper;
	@Mock
	private ReviewRequestValidator requestValidator;
	
	@InjectMocks
	private ReviewGrpcServiceImpl reviewGrpcServiceImpl;
	
	private ReviewDto reviewDto;
	private ReviewProto reviewProto;

	private final Long reviewId = 1L;
	private final String email = "email@email.tk";
	private final Long productId = 1L;
	private final String reviewText = "Excellent";
	private final Integer rating = 5;

	@BeforeEach
	private void setUp(){
		
		reviewDto = new ReviewDto(reviewId, email, productId, reviewText, rating);
		reviewProto = ReviewProto.newBuilder()
				.setReviewId(reviewId)
				.setEmail(email)
				.setProductId(productId)
				.setReviewText(reviewText)
				.setRating(rating)
				.build();		
	}
	
	@Test
	void createReview_shouldReturnReview() throws Exception {

		CreateReviewRequest request = CreateReviewRequest.newBuilder()
				.setProductId(productId)
				.setReviewText(reviewText)
				.setRating(rating)
				.build();
		Mockito.doNothing().when(requestValidator).validateCreateRequest(Mockito.any(CreateReviewRequest.class));
		Mockito.when(reviewMapper.fromCreateToDto(Mockito.any(CreateReviewRequest.class))).thenReturn(reviewDto);
		Mockito.when(reviewServiceAdapter.save(Mockito.any(ReviewDto.class))).thenReturn(Optional.of(reviewDto));
		Mockito.when(reviewMapper.fromDtoToProto(Mockito.any(ReviewDto.class))).thenReturn(reviewProto);
		StreamRecorder<ReviewProto> responseObserver = StreamRecorder.create();
		
		reviewGrpcServiceImpl.createReview(request, responseObserver);		

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<ReviewProto> results = responseObserver.getValues();
        assertEquals(1, results.size());
        ReviewProto revP = results.get(0);
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(revP).isNotNull()
				);
	}

	@Test
	void updateReview_shouldReturnReview() throws Exception {

		UpdateReviewRequest request = UpdateReviewRequest.newBuilder()
				.setReviewId(reviewId)
				.setReviewText(reviewText)
				.setRating(rating)
				.build();
		Mockito.doNothing().when(requestValidator).validateUpdateRequest(Mockito.any(UpdateReviewRequest.class));
		Mockito.when(reviewMapper.fromUpdateToDto(Mockito.any(UpdateReviewRequest.class))).thenReturn(reviewDto);
		Mockito.when(reviewServiceAdapter.update(Mockito.any(ReviewDto.class))).thenReturn(Optional.of(reviewDto));
		Mockito.when(reviewMapper.fromDtoToProto(Mockito.any(ReviewDto.class))).thenReturn(reviewProto);
		StreamRecorder<ReviewProto> responseObserver = StreamRecorder.create();
		
		reviewGrpcServiceImpl.updateReview(request, responseObserver);		

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<ReviewProto> results = responseObserver.getValues();
        assertEquals(1, results.size());
        ReviewProto revP = results.get(0);
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(revP).isNotNull()
				);
	}

	@Test
	void deleteReview_shouldReturnTrue() throws Exception {

		DeleteReviewRequest request = DeleteReviewRequest.newBuilder()
				.setReviewId(reviewId)
				.build();
		Mockito.doNothing().when(requestValidator).validateDeleteRequest(Mockito.any(DeleteReviewRequest.class));
		Mockito.when(reviewServiceAdapter.deleteById(Mockito.anyLong())).thenReturn(true);
		StreamRecorder<DeleteReviewResponse> responseObserver = StreamRecorder.create();
		
		reviewGrpcServiceImpl.deleteReview(request, responseObserver);		

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }

        assertNull(responseObserver.getError());
        List<DeleteReviewResponse> results = responseObserver.getValues();
        assertEquals(1, results.size());
        DeleteReviewResponse response = results.get(0);
		boolean result = response.getSuccess();
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(result).isTrue()
				);		
		
	}

	@Test
	void listReviews_shouldReturnList() throws Exception {

		Mockito.when(reviewServiceAdapter.findAll()).thenReturn(List.of(reviewDto));
		Mockito.when(reviewMapper.fromListDtoToListProto(Mockito.anyList())).thenReturn(List.of(reviewProto));
		StreamRecorder<ListReviewsResponse> responseObserver = StreamRecorder.create();
		
		reviewGrpcServiceImpl.listReviews(Empty.getDefaultInstance(), responseObserver);
        
		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
			fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<ListReviewsResponse> results = responseObserver.getValues();
        assertEquals(1, results.size());
        ListReviewsResponse response = results.get(0);
        List<ReviewProto> list = response.getReviewsList();    	
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);	
	}

	@Test
	void listReviewsPaginated_shouldReturnList() throws Exception {
		PaginationRequest paginationRequest = PaginationRequest.newBuilder()
				.setPage(0)
				.setSize(2)
				.setSortBy(GlobalConstants.REV_ID)
				.setSortDir("DESC")
				.build();
		Mockito.when(reviewServiceAdapter.findAll(Mockito.any(Pageable.class))).thenReturn(List.of(reviewDto));
		Mockito.when(reviewMapper.fromListDtoToListProto(Mockito.anyList())).thenReturn(List.of(reviewProto));
		StreamRecorder<ListReviewsResponse> responseObserver = StreamRecorder.create();
		
		reviewGrpcServiceImpl.listReviewsPaginated(paginationRequest, responseObserver);
        
		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
			fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<ListReviewsResponse> results = responseObserver.getValues();
        assertEquals(1, results.size());
        ListReviewsResponse response = results.get(0);
        List<ReviewProto> list = response.getReviewsList();    	
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);
	}

	@Test
	void getReview_shouldReturnReview() throws Exception {

		GetReviewRequest request = GetReviewRequest.newBuilder()
				.setReviewId(reviewId)
				.build();
		Mockito.doNothing().when(requestValidator).validateGetRequest(Mockito.any(GetReviewRequest.class));
		Mockito.when(reviewServiceAdapter.findById(Mockito.anyLong())).thenReturn(Optional.of(reviewDto));
		Mockito.when(reviewMapper.fromDtoToProto(Mockito.any(ReviewDto.class))).thenReturn(reviewProto);
		StreamRecorder<ReviewProto> responseObserver = StreamRecorder.create();
		
		reviewGrpcServiceImpl.getReview(request, responseObserver);

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<ReviewProto> results = responseObserver.getValues();
        assertEquals(1, results.size());
        ReviewProto revP = results.get(0);
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(revP).isNotNull()
				);
	}

}
