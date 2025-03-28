package dev.ime.api.validation;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.ime.application.exception.ValidationException;
import dev.proto.CreateUserAppRequest;
import dev.proto.UpdateUserAppRequest;

@ExtendWith(MockitoExtension.class)
class DtoValidatorTest {

	private DtoValidator dtoValidator;
	
	private final Long userAppId = 1L;
	private final String name = "name";
	private final String lastname = "lastname";
	private final String email = "email@email.tk";
	private final String invalidEmail = "email@email";
	
	@BeforeEach
	private void setUp(){
		
		dtoValidator = new DtoValidator();
	}
	
	@Test
	void validateCreateUserAppRequest_shouldValidateRight() {

		CreateUserAppRequest createUserAppRequest = CreateUserAppRequest.newBuilder()
				.setName(name)
				.setLastname(lastname)
				.setEmail(email)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateCreateUserAppRequest(createUserAppRequest));
		
	}

    @Test
    void validateCreateUserAppRequest_invalidEmail_shouldThrowException() {
    	
    	CreateUserAppRequest invalidRequest = CreateUserAppRequest.newBuilder()
				.setName(name)
				.setLastname(lastname)
				.setEmail(invalidEmail)
				.build();
        
        ValidationException exception = org.junit.jupiter.api.Assertions.assertThrows(ValidationException.class, 
            () -> dtoValidator.validateCreateUserAppRequest(invalidRequest));
        
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(exception).isNotNull()
				);         
    }

	@Test
	void validateUpdateUserAppRequest_shouldValidateRight() {

		UpdateUserAppRequest updateUserAppRequest = UpdateUserAppRequest.newBuilder()
				.setUserAppId(userAppId)
				.setName(name)
				.setLastname(lastname)
				.setEmail(email)
				.build();
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> dtoValidator.validateUpdateUserAppRequest(updateUserAppRequest));
		
	}

}
