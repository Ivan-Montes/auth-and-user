package dev.ime.api.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.proto.CreateProductRequest;
import dev.proto.DeleteProductRequest;
import dev.proto.GetProductRequest;
import dev.proto.UpdateProductRequest;

@ExtendWith(MockitoExtension.class)
class ProductRequestValidatorTest {

	private ProductRequestValidator dtoValidator;
	
	private final Long productId = 1L;
	private final String productName = "Tomatoes";
	private final String productDescription = "full of red";
	private final Long categoryId = 1L;
	
	@BeforeEach
	private void setUp(){
		
		dtoValidator = new ProductRequestValidator();
	}	

	@Test
	void validateCreateRequest_shouldValidateRight() {
		
		CreateProductRequest request = CreateProductRequest.newBuilder()
				.setProductName(productName)
				.setProductDescription(productDescription)
				.setCategoryId(categoryId)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateCreateRequest(request));
	}

	@Test
	void validateUpdateRequest_shouldValidateRight() {
		
		UpdateProductRequest request = UpdateProductRequest.newBuilder()
				.setProductId(productId)
				.setProductName(productName)
				.setProductDescription(productDescription)
				.setCategoryId(categoryId)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateUpdateRequest(request));
	}

	@Test
	void validateDeleteRequest_shouldValidateRight() {
		
		DeleteProductRequest request = DeleteProductRequest.newBuilder()
				.setProductId(productId)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateDeleteRequest(request));
	}

	@Test
	void validateGetRequest_shouldValidateRight() {
		
		GetProductRequest request = GetProductRequest.newBuilder()
				.setProductId(productId)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateGetRequest(request));
	}

}
