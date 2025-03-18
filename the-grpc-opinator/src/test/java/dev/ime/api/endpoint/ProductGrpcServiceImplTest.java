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

import dev.ime.api.validation.ProductRequestValidator;
import dev.ime.application.dto.ProductDto;
import dev.ime.common.config.GlobalConstants;
import dev.ime.common.mapper.ProductMapper;
import dev.ime.domain.port.inbound.GenericServicePort;
import dev.proto.CreateProductRequest;
import dev.proto.DeleteProductRequest;
import dev.proto.DeleteProductResponse;
import dev.proto.GetProductRequest;
import dev.proto.ListProductsResponse;
import dev.proto.PaginationRequest;
import dev.proto.ProductProto;
import dev.proto.UpdateProductRequest;
import io.grpc.internal.testing.StreamRecorder;

@ExtendWith(MockitoExtension.class)
class ProductGrpcServiceImplTest {

	@Mock
	private GenericServicePort<ProductDto> productServiceAdapter;
	@Mock
	private ProductMapper productMapper;
	@Mock
	private ProductRequestValidator requestValidator;
	@InjectMocks
	private ProductGrpcServiceImpl productGrpcServiceImpl;
	
	private ProductDto productDto;
	private ProductProto productProto;

	private final Long productId = 1L;
	private final String productName = "Tomatoes";
	private final String productDescription = "full of red";
	private final Long categoryId = 1L;

	@BeforeEach
	private void setUp() {
		
		productDto = new ProductDto(productId,productName,productDescription,categoryId);
		productProto = ProductProto.newBuilder()
				.setProductId(productId)
				.setProductName(productName)
				.setProductDescription(productDescription)
				.setCategoryId(categoryId)
				.build();
		
	}
	
	@Test
	void createProduct_shouldReturnProduct() throws Exception {

		CreateProductRequest request = CreateProductRequest.newBuilder()
				.setProductName(productName)
				.setProductDescription(productDescription)
				.setCategoryId(categoryId)
				.build();
		Mockito.doNothing().when(requestValidator).validateCreateRequest(Mockito.any(CreateProductRequest.class));
		Mockito.when(productMapper.fromCreateToDto(Mockito.any(CreateProductRequest.class))).thenReturn(productDto);
		Mockito.when(productServiceAdapter.save(Mockito.any(ProductDto.class))).thenReturn(Optional.of(productDto));
		Mockito.when(productMapper.fromDtoToProto(Mockito.any(ProductDto.class))).thenReturn(productProto);
		StreamRecorder<ProductProto> responseObserver = StreamRecorder.create();
		
		productGrpcServiceImpl.createProduct(request, responseObserver);		

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<ProductProto> results = responseObserver.getValues();
        assertEquals(1, results.size());
        ProductProto proP = results.get(0);
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(proP).isNotNull()
				);
	}

	@Test
	void updateProduct_shouldReturnProduct() throws Exception {

		UpdateProductRequest request = UpdateProductRequest.newBuilder()
				.setProductId(productId)
				.setProductName(productName)
				.setProductDescription(productDescription)
				.setCategoryId(categoryId)
				.build();
		Mockito.doNothing().when(requestValidator).validateUpdateRequest(Mockito.any(UpdateProductRequest.class));
		Mockito.when(productMapper.fromUpdateToDto(Mockito.any(UpdateProductRequest.class))).thenReturn(productDto);
		Mockito.when(productServiceAdapter.update(Mockito.any(ProductDto.class))).thenReturn(Optional.of(productDto));
		Mockito.when(productMapper.fromDtoToProto(Mockito.any(ProductDto.class))).thenReturn(productProto);
		StreamRecorder<ProductProto> responseObserver = StreamRecorder.create();
		
		productGrpcServiceImpl.updateProduct(request, responseObserver);		

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<ProductProto> results = responseObserver.getValues();
        assertEquals(1, results.size());
        ProductProto proP = results.get(0);
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(proP).isNotNull()
				);		
	}

	@Test
	void deletePoduct_shouldReturnTrue() throws Exception {

		DeleteProductRequest request = DeleteProductRequest.newBuilder()
				.setProductId(productId)
				.build();
		Mockito.doNothing().when(requestValidator).validateDeleteRequest(Mockito.any(DeleteProductRequest.class));
		Mockito.when(productServiceAdapter.deleteById(Mockito.anyLong())).thenReturn(true);
		StreamRecorder<DeleteProductResponse> responseObserver = StreamRecorder.create();
		
		productGrpcServiceImpl.deleteProduct(request, responseObserver);		

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }

        assertNull(responseObserver.getError());
        List<DeleteProductResponse> results = responseObserver.getValues();
        assertEquals(1, results.size());
        DeleteProductResponse response = results.get(0);
		boolean result = response.getSuccess();
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(result).isTrue()
				);		
	}

	@Test
	void listProducts_shouldReturnList() throws Exception {

		Mockito.when(productServiceAdapter.findAll()).thenReturn(List.of(productDto));
		Mockito.when(productMapper.fromListDtoToListProto(Mockito.anyList())).thenReturn(List.of(productProto));
		StreamRecorder<ListProductsResponse> responseObserver = StreamRecorder.create();
		
		productGrpcServiceImpl.listProducts(Empty.getDefaultInstance(), responseObserver);
        
		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
			fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<ListProductsResponse> results = responseObserver.getValues();
        assertEquals(1, results.size());
        ListProductsResponse response = results.get(0);
        List<ProductProto> list = response.getProductsList();    	
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);	
	}

	@Test
	void listProductsPaginated_shouldReturnList() throws Exception {
		PaginationRequest paginationRequest = PaginationRequest.newBuilder()
				.setPage(0)
				.setSize(2)
				.setSortBy(GlobalConstants.PROD_NAME)
				.setSortDir("DESC")
				.build();
		Mockito.when(productServiceAdapter.findAll(Mockito.any(Pageable.class))).thenReturn(List.of(productDto));
		Mockito.when(productMapper.fromListDtoToListProto(Mockito.anyList())).thenReturn(List.of(productProto));
		StreamRecorder<ListProductsResponse> responseObserver = StreamRecorder.create();
		
		productGrpcServiceImpl.listProductsPaginated(paginationRequest, responseObserver);
        
		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
			fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<ListProductsResponse> results = responseObserver.getValues();
        assertEquals(1, results.size());
        ListProductsResponse response = results.get(0);
        List<ProductProto> list = response.getProductsList();    	
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);	
	}

	@Test
	void getProduct_shouldReturnProduct() throws Exception {

		GetProductRequest request = GetProductRequest.newBuilder()
				.setProductId(productId)
				.build();
		Mockito.doNothing().when(requestValidator).validateGetRequest(Mockito.any(GetProductRequest.class));
		Mockito.when(productServiceAdapter.findById(Mockito.anyLong())).thenReturn(Optional.of(productDto));
		Mockito.when(productMapper.fromDtoToProto(Mockito.any(ProductDto.class))).thenReturn(productProto);
		StreamRecorder<ProductProto> responseObserver = StreamRecorder.create();
		
		productGrpcServiceImpl.getProduct(request, responseObserver);

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<ProductProto> results = responseObserver.getValues();
        assertEquals(1, results.size());
        ProductProto prodP = results.get(0);
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(prodP).isNotNull()
				);		
	}

}
