package dev.ime.api.error;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import dev.ime.application.dto.ErrorResponse;
import dev.ime.application.exception.BasicException;

@ExtendWith(MockitoExtension.class)
class ApiExceptionHandlerTest {

	private ApiExceptionHandler apiExceptionHandler;	
	private final String name = "Our Exception";
	private final String description = "Our Exception, born and raised here";
	private final UUID uuid = UUID.randomUUID();
	private Map<String, String> errors;	

	@BeforeEach
	private void setUp(){
		apiExceptionHandler = new ApiExceptionHandler();
		errors =  new HashMap<>();
	}	

	@Test
	void handleBasicExceptionExtendedClasses_shouldReturnRelatedInfo() {
		
		BasicException ex = new BasicException(uuid, name, description, errors);
		
		ResponseEntity<ErrorResponse> responseEntity = apiExceptionHandler.handleBasicExceptionExtendedClasses(ex);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(responseEntity).isNotNull(),
				()-> Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
				);
	}

	@Test
	void lastExceptionStands_shouldReturnRelatedInfo() {

		Exception ex = new Exception(description);
		
		ResponseEntity<ErrorResponse> responseEntity = apiExceptionHandler.lastExceptionStands(ex);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(responseEntity).isNotNull(),
				()-> Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
				);
	}		

}
