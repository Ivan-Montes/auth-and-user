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

import dev.ime.api.validation.CategoryRequestValidator;
import dev.ime.application.dto.CategoryDto;
import dev.ime.application.dto.PaginationDto;
import dev.ime.application.exception.EmptyResponseException;
import dev.ime.common.config.GlobalConstants;
import dev.ime.common.mapper.CategoryMapper;
import dev.ime.domain.port.inbound.GenericServicePort;
import dev.proto.CategoryGrpcServiceGrpc;
import dev.proto.CategoryProto;
import dev.proto.CreateCategoryRequest;
import dev.proto.DeleteCategoryRequest;
import dev.proto.DeleteCategoryResponse;
import dev.proto.GetCategoryRequest;
import dev.proto.ListCategoriesResponse;
import dev.proto.PaginationRequest;
import dev.proto.UpdateCategoryRequest;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class CategoryGrpcServiceImpl extends CategoryGrpcServiceGrpc.CategoryGrpcServiceImplBase {

	private final GenericServicePort<CategoryDto> categoryServiceAdapter;
	private final CategoryMapper categoryMapper;
	private final CategoryRequestValidator requestValidator;
	private static final Logger logger = LoggerFactory.getLogger(CategoryGrpcServiceImpl.class);

	public CategoryGrpcServiceImpl(GenericServicePort<CategoryDto> categoryServiceAdapter,
			CategoryMapper categoryMapper, CategoryRequestValidator requestValidator) {
		super();
		this.categoryServiceAdapter = categoryServiceAdapter;
		this.categoryMapper = categoryMapper;
		this.requestValidator = requestValidator;
	}

	@Override
	public void createCategory(CreateCategoryRequest request, StreamObserver<CategoryProto> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.CREATE_CAT, request);
		
		requestValidator.validateCreateRequest(request);

		CategoryProto categoryProto = Optional.ofNullable(request)
				.map(categoryMapper::fromCreateToDto)
				.flatMap(categoryServiceAdapter::save)
				.map(categoryMapper::fromDtoToProto)
				.map(item -> {
					logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.CAT_CREATED, item);
					return item;
				}).orElseThrow(
						() -> new EmptyResponseException(Map.of(GlobalConstants.CAT_CAT, GlobalConstants.MSG_NODATA)));

		responseObserver.onNext(categoryProto);
		responseObserver.onCompleted();
	}

	@Override
	public void updateCategory(UpdateCategoryRequest request, StreamObserver<CategoryProto> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.UPDATE_CAT, request);
		requestValidator.validateUpdateRequest(request);
		CategoryDto dto = categoryMapper.fromUpdateToDto(request);
		Optional<CategoryDto> opt = categoryServiceAdapter.update(dto);
		CategoryProto categoryProto = opt.map(categoryMapper::fromDtoToProto).orElseThrow(
				() -> new EmptyResponseException(Map.of(GlobalConstants.CAT_CAT, GlobalConstants.MSG_NODATA)));
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.CAT_UPDATED, categoryProto);

		responseObserver.onNext(categoryProto);
		responseObserver.onCompleted();
	}

	@Override
	public void deleteCategory(DeleteCategoryRequest request, StreamObserver<DeleteCategoryResponse> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.DELETE_CAT, request);
		
		requestValidator.validateDeleteRequest(request);
		
		DeleteCategoryResponse deleteCategoryResponse = Optional.ofNullable(request.getCategoryId())
				.map(categoryServiceAdapter::deleteById)
				.map(b -> DeleteCategoryResponse.newBuilder().setSuccess(b).build())
				.map(item -> {
					logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.CAT_DELETED, item);
					return item;
				}).orElseThrow(
						() -> new EmptyResponseException(Map.of(GlobalConstants.CAT_CAT, GlobalConstants.MSG_NODATA)));

		responseObserver.onNext(deleteCategoryResponse);
		responseObserver.onCompleted();
	}

	@Override
	public void listCategories(Empty request, StreamObserver<ListCategoriesResponse> responseObserver) {

		List<CategoryProto> protoList = categoryMapper.fromListDtoToListProto(categoryServiceAdapter.findAll());
		ListCategoriesResponse listResponse = ListCategoriesResponse.newBuilder().addAllCategories(protoList).build();

		responseObserver.onNext(listResponse);
		responseObserver.onCompleted();
	}

	@Override
	public void listCategoriesPaginated(PaginationRequest request, StreamObserver<ListCategoriesResponse> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, request.getClass().getSimpleName(), request);
		
		PaginationDto paginationDto = createPaginationDto(request);
		PageRequest pageRequest = createPageable(paginationDto);
		List<CategoryProto> protoList = categoryMapper.fromListDtoToListProto(categoryServiceAdapter.findAll(pageRequest));
		ListCategoriesResponse listResponse = ListCategoriesResponse.newBuilder().addAllCategories(protoList).build();

		responseObserver.onNext(listResponse);
		responseObserver.onCompleted();
	}

	private PaginationDto createPaginationDto(PaginationRequest request) {
		
		Set<String> attrsSet = Set.of(GlobalConstants.CAT_ID,GlobalConstants.CAT_NAME);
		Integer page = Optional.ofNullable(request.getPage()).filter( i -> i >= 0).orElse(0);
    	Integer size = Optional.ofNullable(request.getSize()).filter( i -> i > 0).orElse(100);
        String sortBy = Optional.ofNullable(request.getSortBy()).filter(attrsSet::contains).orElse(GlobalConstants.CAT_ID);
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
	public void getCategory(GetCategoryRequest request, StreamObserver<CategoryProto> responseObserver) {

		requestValidator.validateGetRequest(request);
		
		CategoryProto categoryProto = Optional.ofNullable(request.getCategoryId())
				.flatMap(categoryServiceAdapter::findById)
				.map(categoryMapper::fromDtoToProto)
				.map(item -> {
					logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.CAT_CAT, item);
					return item;
				})
				.orElseThrow(
				() -> new EmptyResponseException(Map.of(GlobalConstants.CAT_CAT, GlobalConstants.MSG_NODATA)));

		responseObserver.onNext(categoryProto);
		responseObserver.onCompleted();
	}

}
