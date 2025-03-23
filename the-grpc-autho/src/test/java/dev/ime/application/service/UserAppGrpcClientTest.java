package dev.ime.application.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.ime.application.dto.UserAppDto;
import dev.proto.CreateUserAppRequest;
import dev.proto.UserAppCreatedResponse;
import dev.proto.UserAppGrpcServiceGrpc;
@ExtendWith(MockitoExtension.class)
class UserAppGrpcClientTest {

	@Mock
	private UserAppGrpcServiceGrpc.UserAppGrpcServiceBlockingStub blockingStub;

	@InjectMocks
	private UserAppGrpcClient userAppGrpcClient;
	
	private UserAppDto userAppDto;
	
	private final Long userId = 1L;
	private final String email = "email@email.tk";
	private final String name = "name";
	private final String lastname = "lastname";
	
	@BeforeEach
	private void setUp(){		
		
		userAppDto = new UserAppDto(userId,email,name,lastname);	
	}		
	
	@Test
	void createUser_shouldReturnTrue() {
		
		UserAppCreatedResponse userAppCreatedResponse = UserAppCreatedResponse.newBuilder().setResult(true).build();		
		Mockito.when(blockingStub.createUser(Mockito.any(CreateUserAppRequest.class))).thenReturn(userAppCreatedResponse);
		
		Boolean bool = userAppGrpcClient.createUser(userAppDto);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertTrue(bool)
				);
	}

}
