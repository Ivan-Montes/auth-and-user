package dev.ime.api.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.proto.CreateCategoryRequest;
import dev.proto.DeleteCategoryRequest;
import dev.proto.GetCategoryRequest;
import dev.proto.UpdateCategoryRequest;

@ExtendWith(MockitoExtension.class)
class CategoryRequestValidatorTest {

	private CategoryRequestValidator dtoValidator;
	
	private final Long categoryId = 1L;
	private final String categoryName = "Vegetables";
	
	@BeforeEach
	private void setUp(){
		
		dtoValidator = new CategoryRequestValidator();
	}
	
	@Test
	void validateCreateRequest_shouldValidateRight() {
		
		CreateCategoryRequest request = CreateCategoryRequest.newBuilder()
				.setCategoryName(categoryName)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateCreateRequest(request));

	}

	@Test
	void validateUpdateRequest_shouldValidateRight() {
		
		UpdateCategoryRequest request = UpdateCategoryRequest.newBuilder()
				.setCategoryId(categoryId)
				.setCategoryName(categoryName)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateUpdateRequest(request));

	}

	@Test
	void validateDeleteRequest_shouldValidateRight() {
		
		DeleteCategoryRequest request = DeleteCategoryRequest.newBuilder()
				.setCategoryId(categoryId)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateDeleteRequest(request));

	}

	@Test
	void validateGetRequest_shouldValidateRight() {
		
		GetCategoryRequest request = GetCategoryRequest.newBuilder()
				.setCategoryId(categoryId)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateGetRequest(request));

	}

}
