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

import dev.ime.api.validation.DtoValidator;
import dev.ime.application.dto.UserAppDto;
import dev.ime.common.config.GlobalConstants;
import dev.ime.common.mapper.UserAppMapper;
import dev.ime.domain.port.inbound.UserAppServicePort;
import dev.proto.CreateUserAppRequest;
import dev.proto.ListUsersAppResponse;
import dev.proto.PaginationRequest;
import dev.proto.UpdateUserAppRequest;
import dev.proto.UserAppCreatedResponse;
import dev.proto.UserAppProto;
import io.grpc.internal.testing.StreamRecorder;

@ExtendWith(MockitoExtension.class)
class UserAppGrpcServiceImplTest {

	@Mock
	private UserAppServicePort<UserAppDto> userAppServiceAdapter;
	@Mock
	private UserAppMapper userAppMapper;
	@Mock
	private DtoValidator dtoValidator;

	@InjectMocks
	private UserAppGrpcServiceImpl userAppGrpcServiceImpl;
	
	private UserAppProto userAppProto;
	private UserAppDto userAppDto;
	private CreateUserAppRequest createUserAppRequest;
	private UpdateUserAppRequest updateUserAppRequest;
	private PaginationRequest paginationRequest;
	
	
	private final Long userId = 1L;
	private final String email = "email@email.tk";
	private final String name = "name";
	private final String lastname = "lastname";

	@BeforeEach
	private void setUp(){
		
		userAppProto = UserAppProto.newBuilder()
				.setUserAppId(userId)
				.setEmail(email)
				.setName(name)
				.setLastname(lastname)
				.build();
		
		userAppDto = new UserAppDto(userId, email, name, lastname);
		
		paginationRequest= PaginationRequest.newBuilder()
				.setPage(0)
				.setSize(2)
				.setSortBy("email")
				.setSortDir("DESC")
				.build();
		
		createUserAppRequest = CreateUserAppRequest.newBuilder()
				.setEmail(email)
				.setName(name)
				.setLastname(lastname)
				.build();
		
		updateUserAppRequest = UpdateUserAppRequest.newBuilder()
				.setUserAppId(userId)
				.setEmail(email)
				.setName(name)
				.setLastname(lastname)
				.build();
	}
		

	@Test
	void listUsers_shouldReturnList() throws Exception {
		
		Mockito.when(userAppServiceAdapter.findAll()).thenReturn(List.of(userAppDto));
		Mockito.when(userAppMapper.fromListUserAppDtoToListUserAppProto(Mockito.anyList())).thenReturn(List.of(userAppProto));
		StreamRecorder<ListUsersAppResponse> responseObserver = StreamRecorder.create();
		
		userAppGrpcServiceImpl.listUsers(Empty.getDefaultInstance(), responseObserver);
        
		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
			fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<ListUsersAppResponse> results = responseObserver.getValues();
        assertEquals(1, results.size());
        ListUsersAppResponse response = results.get(0);
        List<UserAppProto> list = response.getUsersList();    	
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);	
	}

	@Test
	void listUsers_withPagination_shouldReturnList() throws Exception {
		
		Mockito.when(userAppServiceAdapter.findAll(Mockito.any(Pageable.class))).thenReturn(List.of(userAppDto));
		Mockito.when(userAppMapper.fromListUserAppDtoToListUserAppProto(Mockito.anyList())).thenReturn(List.of(userAppProto));
		StreamRecorder<ListUsersAppResponse> responseObserver = StreamRecorder.create();
		
		userAppGrpcServiceImpl.listUsersPaginated(paginationRequest, responseObserver);
        
		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<ListUsersAppResponse> results = responseObserver.getValues();
        assertEquals(1, results.size());
        ListUsersAppResponse response = results.get(0);
        List<UserAppProto> list = response.getUsersList();    	
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);	
	}

	@Test
	void createUser_shouldReturnTrue() throws Exception {
		
		Mockito.doNothing().when(dtoValidator).validateCreateUserAppRequest(Mockito.any(CreateUserAppRequest.class));
		Mockito.when(userAppMapper.fromCreateUserAppRequestToUserAppDto(Mockito.any(CreateUserAppRequest.class))).thenReturn(userAppDto);
		Mockito.when(userAppServiceAdapter.create(Mockito.any(UserAppDto.class))).thenReturn(true);
		StreamRecorder<UserAppCreatedResponse> responseObserver = StreamRecorder.create();

		userAppGrpcServiceImpl.createUser(createUserAppRequest, responseObserver);

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }

        assertNull(responseObserver.getError());
        List<UserAppCreatedResponse> results = responseObserver.getValues();
        assertEquals(1, results.size());
        UserAppCreatedResponse response = results.get(0);
		boolean result = response.getResult();
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(result).isTrue()
				);		
	}

	@Test
	void updateUser_shouldReturnUpdatedUser() throws Exception {
		
		Mockito.doNothing().when(dtoValidator).validateUpdateUserAppRequest(Mockito.any(UpdateUserAppRequest.class));
		Mockito.when(userAppMapper.fromUpdateUserAppRequestToUserAppDto(Mockito.any(UpdateUserAppRequest.class))).thenReturn(userAppDto);
		Mockito.when(userAppServiceAdapter.update(Mockito.any(UserAppDto.class))).thenReturn(Optional.of(userAppDto));
		Mockito.when(userAppMapper.fromUserAppDtoToUserAppProto(Mockito.any(UserAppDto.class))).thenReturn(userAppProto);
		StreamRecorder<UserAppProto> responseObserver = StreamRecorder.create();

		userAppGrpcServiceImpl.updateUser(updateUserAppRequest, responseObserver);

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<UserAppProto> results = responseObserver.getValues();
        assertEquals(1, results.size());
        UserAppProto userP = results.get(0);
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(userP).isNotNull()
				);
	}


}
