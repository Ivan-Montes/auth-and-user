package dev.ime.api.validation;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.ime.application.exception.ValidationException;
import dev.proto.CreateUserRequest;

@ExtendWith(MockitoExtension.class)
class DtoValidatorTest {

	private DtoValidator dtoValidator;
	
	private final String name = "name";
	private final String lastname = "lastname";
	private final String email = "email@email.tk";
	private final String invalidEmail = "email@email";
	private final String password = "password";
	
	@BeforeEach
	private void setUp(){
		
		dtoValidator = new DtoValidator();
	}
	
	@Test
	void validateCreateUserRequest_shouldValidateRight() {

		CreateUserRequest createUserRequest = CreateUserRequest.newBuilder()
				.setName(name)
				.setLastname(lastname)
				.setEmail(email)
				.setPassword(password)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateCreateUserRequest(createUserRequest));
		
	}

    @Test
    void validateCreateUserRequest_invalidEmail_shouldThrowException() {
    	
        CreateUserRequest invalidRequest = CreateUserRequest.newBuilder()
				.setName(name)
				.setLastname(lastname)
				.setEmail(invalidEmail)
				.setPassword(password)
				.build();
        
        ValidationException exception = org.junit.jupiter.api.Assertions.assertThrows(ValidationException.class, 
            () -> dtoValidator.validateCreateUserRequest(invalidRequest));
        
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(exception).isNotNull()
				);         
    }
    
}
