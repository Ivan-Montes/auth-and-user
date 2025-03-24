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

import dev.ime.api.validation.CategoryRequestValidator;
import dev.ime.application.dto.CategoryDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.CategoryMapper;
import dev.ime.domain.port.inbound.GenericServicePort;
import dev.proto.CategoryProto;
import dev.proto.CreateCategoryRequest;
import dev.proto.DeleteCategoryRequest;
import dev.proto.DeleteCategoryResponse;
import dev.proto.GetCategoryRequest;
import dev.proto.ListCategoriesResponse;
import dev.proto.PaginationRequest;
import dev.proto.UpdateCategoryRequest;
import io.grpc.internal.testing.StreamRecorder;

@ExtendWith(MockitoExtension.class)
class CategoryGrpcServiceImplTest {

	@Mock
	private GenericServicePort<CategoryDto> categoryServiceAdapter;
	@Mock
	private CategoryMapper categoryMapper;
	@Mock
	private CategoryRequestValidator requestValidator;

	@InjectMocks
	private CategoryGrpcServiceImpl categoryGrpcServiceImpl;

	private CategoryDto categoryDto;
	private CategoryProto categoryProto;
	
	private final Long categoryId = 1L;
	private final String categoryName = "Vegetables";

	@BeforeEach
	private void setUp() {
		
		categoryDto = new CategoryDto(null,categoryName);
		categoryProto = CategoryProto.newBuilder()
				.setCategoryId(categoryId)
				.setCategoryName(categoryName)
				.build();
	}
		
	@Test
	void createCategory_shouldReturnCategory() throws Exception {

		CreateCategoryRequest request = CreateCategoryRequest.newBuilder()
				.setCategoryName(categoryName)
				.build();
		Mockito.doNothing().when(requestValidator).validateCreateRequest(Mockito.any(CreateCategoryRequest.class));
		Mockito.when(categoryMapper.fromCreateToDto(Mockito.any(CreateCategoryRequest.class))).thenReturn(categoryDto);
		Mockito.when(categoryServiceAdapter.save(Mockito.any(CategoryDto.class))).thenReturn(Optional.of(categoryDto));
		Mockito.when(categoryMapper.fromDtoToProto(Mockito.any(CategoryDto.class))).thenReturn(categoryProto);
		StreamRecorder<CategoryProto> responseObserver = StreamRecorder.create();
		
		categoryGrpcServiceImpl.createCategory(request, responseObserver);		

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<CategoryProto> results = responseObserver.getValues();
        assertEquals(1, results.size());
        CategoryProto catP = results.get(0);
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(catP).isNotNull()
				);
	}

	@Test
	void updateCategory_shouldReturnCategory() throws Exception {

		UpdateCategoryRequest request = UpdateCategoryRequest.newBuilder()
				.setCategoryId(categoryId)
				.setCategoryName(categoryName)
				.build();
		Mockito.doNothing().when(requestValidator).validateUpdateRequest(Mockito.any(UpdateCategoryRequest.class));
		Mockito.when(categoryMapper.fromUpdateToDto(Mockito.any(UpdateCategoryRequest.class))).thenReturn(categoryDto);
		Mockito.when(categoryServiceAdapter.update(Mockito.any(CategoryDto.class))).thenReturn(Optional.of(categoryDto));
		Mockito.when(categoryMapper.fromDtoToProto(Mockito.any(CategoryDto.class))).thenReturn(categoryProto);
		StreamRecorder<CategoryProto> responseObserver = StreamRecorder.create();
		
		categoryGrpcServiceImpl.updateCategory(request, responseObserver);		

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<CategoryProto> results = responseObserver.getValues();
        assertEquals(1, results.size());
        CategoryProto catP = results.get(0);
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(catP).isNotNull()
				);
	}
	
	@Test
	void deleteCategory_shouldReturnTrue() throws Exception {

		DeleteCategoryRequest request = DeleteCategoryRequest.newBuilder()
				.setCategoryId(categoryId)
				.build();
		Mockito.doNothing().when(requestValidator).validateDeleteRequest(Mockito.any(DeleteCategoryRequest.class));
		Mockito.when(categoryServiceAdapter.deleteById(Mockito.anyLong())).thenReturn(true);
		StreamRecorder<DeleteCategoryResponse> responseObserver = StreamRecorder.create();
		
		categoryGrpcServiceImpl.deleteCategory(request, responseObserver);		

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }

        assertNull(responseObserver.getError());
        List<DeleteCategoryResponse> results = responseObserver.getValues();
        assertEquals(1, results.size());
        DeleteCategoryResponse response = results.get(0);
		boolean result = response.getSuccess();
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(result).isTrue()
				);		
	}

	@Test
	void listCategories_shouldReturnList() throws Exception {
		
		Mockito.when(categoryServiceAdapter.findAll()).thenReturn(List.of(categoryDto));
		Mockito.when(categoryMapper.fromListDtoToListProto(Mockito.anyList())).thenReturn(List.of(categoryProto));
		StreamRecorder<ListCategoriesResponse> responseObserver = StreamRecorder.create();
		
		categoryGrpcServiceImpl.listCategories(Empty.getDefaultInstance(), responseObserver);
        
		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
			fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<ListCategoriesResponse> results = responseObserver.getValues();
        assertEquals(1, results.size());
        ListCategoriesResponse response = results.get(0);
        List<CategoryProto> list = response.getCategoriesList();    	
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);	
	}	

	@Test
	void listCategoriesPaginated_shouldReturnList() throws Exception {
		PaginationRequest paginationRequest = PaginationRequest.newBuilder()
				.setPage(0)
				.setSize(2)
				.setSortBy(GlobalConstants.CAT_NAME)
				.setSortDir("DESC")
				.build();
		Mockito.when(categoryServiceAdapter.findAll(Mockito.any(Pageable.class))).thenReturn(List.of(categoryDto));
		Mockito.when(categoryMapper.fromListDtoToListProto(Mockito.anyList())).thenReturn(List.of(categoryProto));
		StreamRecorder<ListCategoriesResponse> responseObserver = StreamRecorder.create();
		
		categoryGrpcServiceImpl.listCategoriesPaginated(paginationRequest, responseObserver);
        
		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
			fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<ListCategoriesResponse> results = responseObserver.getValues();
        assertEquals(1, results.size());
        ListCategoriesResponse response = results.get(0);
        List<CategoryProto> list = response.getCategoriesList();    	
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);	
	}		

	@Test
	void getCategory_shouldReturnCategory() throws Exception {

		GetCategoryRequest request = GetCategoryRequest.newBuilder()
				.setCategoryId(categoryId)
				.build();
		Mockito.doNothing().when(requestValidator).validateGetRequest(Mockito.any(GetCategoryRequest.class));
		Mockito.when(categoryServiceAdapter.findById(Mockito.anyLong())).thenReturn(Optional.of(categoryDto));
		Mockito.when(categoryMapper.fromDtoToProto(Mockito.any(CategoryDto.class))).thenReturn(categoryProto);
		StreamRecorder<CategoryProto> responseObserver = StreamRecorder.create();
		
		categoryGrpcServiceImpl.getCategory(request, responseObserver);

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<CategoryProto> results = responseObserver.getValues();
        assertEquals(1, results.size());
        CategoryProto catP = results.get(0);
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(catP).isNotNull()
				);
	}			
		
}
