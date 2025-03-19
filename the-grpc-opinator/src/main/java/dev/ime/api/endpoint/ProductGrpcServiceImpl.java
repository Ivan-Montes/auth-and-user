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

import dev.ime.api.validation.ProductRequestValidator;
import dev.ime.application.dto.PaginationDto;
import dev.ime.application.dto.ProductDto;
import dev.ime.application.exception.EmptyResponseException;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.ProductMapper;
import dev.ime.domain.port.inbound.GenericServicePort;
import dev.proto.CreateProductRequest;
import dev.proto.DeleteProductRequest;
import dev.proto.DeleteProductResponse;
import dev.proto.GetProductRequest;
import dev.proto.ListProductsResponse;
import dev.proto.PaginationRequest;
import dev.proto.ProductGrpcServiceGrpc;
import dev.proto.ProductProto;
import dev.proto.UpdateProductRequest;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class ProductGrpcServiceImpl  extends ProductGrpcServiceGrpc.ProductGrpcServiceImplBase{

	private final GenericServicePort<ProductDto> productServiceAdapter;
	private final ProductMapper productMapper;
	private final ProductRequestValidator requestValidator;
	private static final Logger logger = LoggerFactory.getLogger(ProductGrpcServiceImpl.class);
	
	public ProductGrpcServiceImpl(GenericServicePort<ProductDto> productServiceAdapter, ProductMapper productMapper,
			ProductRequestValidator requestValidator) {
		super();
		this.productServiceAdapter = productServiceAdapter;
		this.productMapper = productMapper;
		this.requestValidator = requestValidator;
	}

	@Override
	public void createProduct(CreateProductRequest request, StreamObserver<ProductProto> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.CREATE_PROD, request);
		
		requestValidator.validateCreateRequest(request);

		ProductProto productProto = Optional.ofNullable(request)
				.map(productMapper::fromCreateToDto)
				.flatMap(productServiceAdapter::save)
				.map(productMapper::fromDtoToProto)
				.map(item -> {
					logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.PROD_CREATED, item);
					return item;
				}).orElseThrow(
						() -> new EmptyResponseException(Map.of(GlobalConstants.PROD_CAT, GlobalConstants.MSG_NODATA)));

		responseObserver.onNext(productProto);
		responseObserver.onCompleted();
	}

	@Override
	public void updateProduct(UpdateProductRequest request, StreamObserver<ProductProto> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.UPDATE_PROD, request);
		requestValidator.validateUpdateRequest(request);
		ProductDto dto = productMapper.fromUpdateToDto(request);
		Optional<ProductDto> opt = productServiceAdapter.update(dto);
		ProductProto productProto = opt.map(productMapper::fromDtoToProto).orElseThrow(
				() -> new EmptyResponseException(Map.of(GlobalConstants.PROD_CAT, GlobalConstants.MSG_NODATA)));
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.PROD_UPDATED, productProto);

		responseObserver.onNext(productProto);
		responseObserver.onCompleted();
	}

	@Override
	public void deleteProduct(DeleteProductRequest request, StreamObserver<DeleteProductResponse> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.DELETE_PROD, request);
		
		requestValidator.validateDeleteRequest(request);
		
		DeleteProductResponse deleteProductResponse = Optional.ofNullable(request.getProductId())
				.map(productServiceAdapter::deleteById)
				.map(b -> DeleteProductResponse.newBuilder().setSuccess(b).build())
				.map(item -> {
					logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.PROD_DELETED, item);
					return item;
				}).orElseThrow(
						() -> new EmptyResponseException(Map.of(GlobalConstants.PROD_CAT, GlobalConstants.MSG_NODATA)));

		responseObserver.onNext(deleteProductResponse);
		responseObserver.onCompleted();
	}

	@Override
	public void listProducts(Empty request, StreamObserver<ListProductsResponse> responseObserver) {

		List<ProductProto> protoList = productMapper.fromListDtoToListProto(productServiceAdapter.findAll());
		ListProductsResponse listResponse = ListProductsResponse.newBuilder().addAllProducts(protoList).build();

		responseObserver.onNext(listResponse);
		responseObserver.onCompleted();
	}

	@Override
	public void listProductsPaginated(PaginationRequest request, StreamObserver<ListProductsResponse> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, request.getClass().getSimpleName(), request);
		
		PaginationDto paginationDto = createPaginationDto(request);
		PageRequest pageRequest = createPageable(paginationDto);
		List<ProductProto> protoList = productMapper.fromListDtoToListProto(productServiceAdapter.findAll(pageRequest));
		ListProductsResponse listResponse = ListProductsResponse.newBuilder().addAllProducts(protoList).build();

		responseObserver.onNext(listResponse);
		responseObserver.onCompleted();
	}

	private PaginationDto createPaginationDto(PaginationRequest request) {
		
		Set<String> attrsSet = Set.of(GlobalConstants.PROD_ID,GlobalConstants.PROD_NAME,GlobalConstants.PROD_DESC);
		Integer page = Optional.ofNullable(request.getPage()).filter( i -> i >= 0).orElse(0);
    	Integer size = Optional.ofNullable(request.getSize()).filter( i -> i > 0).orElse(100);
        String sortBy = Optional.ofNullable(request.getSortBy()).filter(attrsSet::contains).orElse(GlobalConstants.PROD_ID);
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
	public void getProduct(GetProductRequest request, StreamObserver<ProductProto> responseObserver) {

		requestValidator.validateGetRequest(request);
		
		ProductProto productProto = Optional.ofNullable(request.getProductId())
				.flatMap(productServiceAdapter::findById)
				.map(productMapper::fromDtoToProto)
				.map(item -> {
					logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.PROD_CAT, item);
					return item;
				})
				.orElseThrow(
				() -> new EmptyResponseException(Map.of(GlobalConstants.PROD_CAT, GlobalConstants.MSG_NODATA)));

		responseObserver.onNext(productProto);
		responseObserver.onCompleted();
	}

}
