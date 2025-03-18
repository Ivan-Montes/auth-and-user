package dev.ime.api.error;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.ime.application.exception.BasicException;
import io.grpc.Status;
import io.grpc.StatusException;

@ExtendWith(MockitoExtension.class)
class GrpcExceptionAdviceTest {

	private GrpcExceptionAdvice grpcExceptionAdvice;
	
	private final String name = "Our Exception";
	private final String description = "Our Exception, born and raised here";
	private final UUID uuid = UUID.randomUUID();
	private Map<String, String> errors;	

	@BeforeEach
	private void setUp(){
		grpcExceptionAdvice = new GrpcExceptionAdvice();
		errors =  new HashMap<>();
	}
	
	@Test
	void handleBasicExceptionClasses_shouldReturnStatus() {
		
		BasicException ex = new BasicException(uuid, name, description, errors);

		StatusException status = grpcExceptionAdvice.handleBasicExceptionClasses(ex);

		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(status).isNotNull()
				);
	}

	@Test
	void lastExceptionStands_shouldReturnStatus() {

		Exception ex = new Exception(description);
		
		Status status = grpcExceptionAdvice.handleException(ex);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(status).isNotNull()
				);
	}
	
}
