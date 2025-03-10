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
import dev.ime.application.dto.RegisterRequestDto;
import dev.ime.application.dto.UserDto;
import dev.ime.application.service.AuthGrpcServiceAdapter;
import dev.ime.common.config.GlobalConstants;
import dev.ime.common.mapper.UserMapper;
import dev.proto.CreateUserRequest;
import dev.proto.ListUsersResponse;
import dev.proto.PaginationRequest;
import dev.proto.UserProto;
import io.grpc.internal.testing.StreamRecorder;

@ExtendWith(MockitoExtension.class)
class AuthGrpcServiceImplTest {

	@Mock
	private AuthGrpcServiceAdapter authGrpcService;
	@Mock
	private UserMapper userMapper;
	@Mock
	private DtoValidator dtoValidator;

	@InjectMocks
	private AuthGrpcServiceImpl authGrpcServiceImpl;
	
	private UserProto userProto;
	private UserDto userDto;
	private RegisterRequestDto registerRequestDto;
	private CreateUserRequest createUserRequest;
	private PaginationRequest paginationRequest;
	
	private final Long userId = 1L;
	private final String email = "email@email.tk";
	private final String password = "god";
	private final String userRole = "user";
	private final String name = "name";
	private final String lastname = "lastname";

	@BeforeEach
	private void setUp() {
		
		userProto = UserProto.newBuilder()
		.setId(userId)
		.setName(name)
		.setLastname(lastname)
		.setEmail(email)
		.setPassword(password)
		.setRole(userRole)
		.build();
		userDto = new UserDto(userId,email,password,userRole);
		
		paginationRequest= PaginationRequest.newBuilder()
				.setPage(0)
				.setSize(2)
				.setSortBy("email")
				.setSortDir("DESC")
				.build();
		
		registerRequestDto = new RegisterRequestDto(
				name,
				lastname,
				email,
				password
				);	
		
		createUserRequest = CreateUserRequest.newBuilder()
				.setName(name)
				.setLastname(lastname)
				.setEmail(email)
				.setPassword(password)
				.build();
	}
	
	@Test
	void listUsers_shouldReturnList() throws Exception {
		
		Mockito.when(authGrpcService.findAll()).thenReturn(List.of(userDto));
		Mockito.when(userMapper.fromListUserDtoToListUserProto(Mockito.anyList())).thenReturn(List.of(userProto));
		StreamRecorder<ListUsersResponse> responseObserver = StreamRecorder.create();
		
		authGrpcServiceImpl.listUsers(Empty.getDefaultInstance(), responseObserver);
        
		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
			fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<ListUsersResponse> results = responseObserver.getValues();
        assertEquals(1, results.size());
        ListUsersResponse response = results.get(0);
        List<UserProto> list = response.getUsersList();    	
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);	
	}

	@Test
	void listUsers_withPagination_shouldReturnList() throws Exception {
		
		Mockito.when(authGrpcService.findAll(Mockito.any(Pageable.class))).thenReturn(List.of(userDto));
		Mockito.when(userMapper.fromListUserDtoToListUserProto(Mockito.anyList())).thenReturn(List.of(userProto));
		StreamRecorder<ListUsersResponse> responseObserver = StreamRecorder.create();
		
		authGrpcServiceImpl.listUsersPaginated(paginationRequest, responseObserver);
        
		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<ListUsersResponse> results = responseObserver.getValues();
        assertEquals(1, results.size());
        ListUsersResponse response = results.get(0);
        List<UserProto> list = response.getUsersList();    	
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);	
	}

	@Test
	void createUser_shouldReturnCreatedUser() throws Exception {
		
		Mockito.doNothing().when(dtoValidator).validateCreateUserRequest(Mockito.any(CreateUserRequest.class));
		Mockito.when(userMapper.fromRegisterRequestToRegisterDto(Mockito.any(CreateUserRequest.class))).thenReturn(registerRequestDto);
		Mockito.when(authGrpcService.register(Mockito.any(RegisterRequestDto.class))).thenReturn(Optional.of(userDto));
		Mockito.when(userMapper.fromUserDtoToUserProto(Mockito.any(UserDto.class))).thenReturn(userProto);
		StreamRecorder<UserProto> responseObserver = StreamRecorder.create();

		authGrpcServiceImpl.createUser(createUserRequest, responseObserver);

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<UserProto> results = responseObserver.getValues();
        assertEquals(1, results.size());
        UserProto userP = results.get(0);
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(userP).isNotNull()
				);
	}

}
