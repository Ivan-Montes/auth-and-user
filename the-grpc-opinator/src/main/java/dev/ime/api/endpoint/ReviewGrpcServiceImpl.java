package dev.ime.api.endpoint;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.google.protobuf.Empty;

import dev.ime.api.validation.ReviewRequestValidator;
import dev.ime.application.dto.PaginationDto;
import dev.ime.application.dto.ReviewDto;
import dev.ime.application.exception.EmptyResponseException;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.ReviewMapper;
import dev.ime.domain.port.inbound.GenericServicePort;
import dev.proto.CreateReviewRequest;
import dev.proto.DeleteReviewRequest;
import dev.proto.DeleteReviewResponse;
import dev.proto.GetReviewRequest;
import dev.proto.ListReviewsResponse;
import dev.proto.PaginationRequest;
import dev.proto.ReviewGrpcServiceGrpc;
import dev.proto.ReviewProto;
import dev.proto.UpdateReviewRequest;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class ReviewGrpcServiceImpl extends ReviewGrpcServiceGrpc.ReviewGrpcServiceImplBase{

	private final GenericServicePort<ReviewDto> reviewServiceAdapter;
	private final ReviewMapper reviewMapper;
	private final ReviewRequestValidator requestValidator;
	private static final Logger logger = LoggerFactory.getLogger(ReviewGrpcServiceImpl.class);
	
	public ReviewGrpcServiceImpl(GenericServicePort<ReviewDto> reviewServiceAdapter, ReviewMapper reviewMapper,
			ReviewRequestValidator reviewRequestValidator) {
		super();
		this.reviewServiceAdapter = reviewServiceAdapter;
		this.reviewMapper = reviewMapper;
		this.requestValidator = reviewRequestValidator;
	}

	@Override
	public void createReview(CreateReviewRequest request, StreamObserver<ReviewProto> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.CREATE_REV, request);
		
		requestValidator.validateCreateRequest(request);

		ReviewProto reviewProto = Optional.ofNullable(request)
				.map(reviewMapper::fromCreateToDto)
				.flatMap(reviewServiceAdapter::save)
				.map(reviewMapper::fromDtoToProto)
				.map(item -> {
					logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.REV_CREATED, item);
					return item;
				}).orElseThrow(
						() -> new EmptyResponseException(Map.of(GlobalConstants.REV_CAT, GlobalConstants.MSG_NODATA)));

		responseObserver.onNext(reviewProto);
		responseObserver.onCompleted();
	}

	@Override
	public void updateReview(UpdateReviewRequest request, StreamObserver<ReviewProto> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.UPDATE_REV, request);
		requestValidator.validateUpdateRequest(request);
		ReviewDto dto = reviewMapper.fromUpdateToDto(request);
		Optional<ReviewDto> opt = reviewServiceAdapter.update(dto);
		ReviewProto reviewProto = opt.map(reviewMapper::fromDtoToProto).orElseThrow(
				() -> new EmptyResponseException(Map.of(GlobalConstants.REV_CAT, GlobalConstants.MSG_NODATA)));
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.REV_UPDATED, reviewProto);

		responseObserver.onNext(reviewProto);
		responseObserver.onCompleted();
	}

	@Override
	public void deleteReview(DeleteReviewRequest request, StreamObserver<DeleteReviewResponse> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.DELETE_REV, request);
		
		requestValidator.validateDeleteRequest(request);
		
		DeleteReviewResponse deleteReviewResponse = Optional.ofNullable(request.getReviewId())
				.map(reviewServiceAdapter::deleteById)
				.map(b -> DeleteReviewResponse.newBuilder().setSuccess(b).build())
				.map(item -> {
					logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.REV_DELETED, item);
					return item;
				}).orElseThrow(
						() -> new EmptyResponseException(Map.of(GlobalConstants.REV_CAT, GlobalConstants.MSG_NODATA)));

		responseObserver.onNext(deleteReviewResponse);
		responseObserver.onCompleted();
	}

	@Override
	public void listReviews(Empty request, StreamObserver<ListReviewsResponse> responseObserver) {

		List<ReviewProto> protoList = reviewMapper.fromListDtoToListProto(reviewServiceAdapter.findAll());
		ListReviewsResponse listResponse = ListReviewsResponse.newBuilder().addAllReviews(protoList).build();

		responseObserver.onNext(listResponse);
		responseObserver.onCompleted();
	}

	@Override
	public void listReviewsPaginated(PaginationRequest request, StreamObserver<ListReviewsResponse> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, request.getClass().getSimpleName(), request);
		
		PaginationDto paginationDto = createPaginationDto(request);
		PageRequest pageRequest = createPageable(paginationDto);
		List<ReviewProto> protoList = reviewMapper.fromListDtoToListProto(reviewServiceAdapter.findAll(pageRequest));
		ListReviewsResponse listResponse = ListReviewsResponse.newBuilder().addAllReviews(protoList).build();

		responseObserver.onNext(listResponse);
		responseObserver.onCompleted();
	}

	private PaginationDto createPaginationDto(PaginationRequest request) {
		
		Set<String> attrsSet = Set.of(GlobalConstants.REV_ID,GlobalConstants.USER_EMAIL,GlobalConstants.REV_TXT, GlobalConstants.REV_RAT);
		Integer page = Optional.ofNullable(request.getPage()).filter( i -> i >= 0).orElse(0);
    	Integer size = Optional.ofNullable(request.getSize()).filter( i -> i > 0).orElse(100);
        String sortBy = Optional.ofNullable(request.getSortBy()).filter(attrsSet::contains).orElse(GlobalConstants.REV_ID);
        String sortDir = Optional.ofNullable(request.getSortDir())
        		.map(String::toUpperCase)
        		.filter( sorting -> sorting.equals(GlobalConstants.PS_A) || sorting.equals(GlobalConstants.PS_D))
        		.orElse(GlobalConstants.PS_A);
        
        return new PaginationDto(page, size, sortBy, sortDir);
	}
	
	private PageRequest createPageable(PaginationDto paginationDto) {

		Sort sort = Sort.by(Sort.Direction.fromString(paginationDto.sortDir()), paginationDto.sortBy());
		return PageRequest.of(paginationDto.page(), paginationDto.size(), sort);
	}

	@Override
	public void getReview(GetReviewRequest request, StreamObserver<ReviewProto> responseObserver) {

		requestValidator.validateGetRequest(request);
		
		ReviewProto reviewProto = Optional.ofNullable(request.getReviewId())
				.flatMap(reviewServiceAdapter::findById)
				.map(reviewMapper::fromDtoToProto)
				.map(item -> {
					logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.REV_CAT, item);
					return item;
				})
				.orElseThrow(
				() -> new EmptyResponseException(Map.of(GlobalConstants.REV_CAT, GlobalConstants.MSG_NODATA)));

		responseObserver.onNext(reviewProto);
		responseObserver.onCompleted();
	}

}
