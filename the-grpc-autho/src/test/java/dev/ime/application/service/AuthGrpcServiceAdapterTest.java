package dev.ime.application.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import dev.ime.application.dto.RegisterRequestDto;
import dev.ime.application.dto.UserAppDto;
import dev.ime.application.dto.UserDto;
import dev.ime.common.config.GlobalConstants;
import dev.ime.common.mapper.UserMapper;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.Role;
import dev.ime.domain.model.User;
import dev.ime.domain.port.outbound.PublisherPort;
import dev.ime.domain.port.outbound.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthGrpcServiceAdapterTest {

	@Mock
	private UserRepository userRepository;
	@Mock
    private PasswordEncoder passwordEncoder;
	@Mock
    private UserAppGrpcClient userAppGrpcClient;
	@Mock
    private UserMapper userMapper;
	@Mock
	private PublisherPort publisherAdapter;
	
	@InjectMocks
	private AuthGrpcServiceAdapter authGrpcServiceAdapter;

	private User user;
	private UserDto userDto;
	private RegisterRequestDto registerRequestDto;
	private Event event;
	
	private final Long userId = 1L;
	private final String email = "email@email.tk";
	private final String password = "god";
	private final String userRole = "user";
	private final String name = "name";
	private final String lastname = "lastname";
	private final Pageable pageable = PageRequest.of(0, 10, Sort.by("email").ascending());
	
	private UUID eventId;
	private final String eventCategory = GlobalConstants.USER_CAT;
	private final String eventType = GlobalConstants.USER_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private final Map<String, Object> eventData = new HashMap<>();
	
	@BeforeEach
	private void setUp(){
		
		user = new User(userId,email,password,Role.USER);
		userDto = new UserDto(userId,email,password,userRole);
		registerRequestDto = new RegisterRequestDto(name, lastname, email, password);

		eventId = UUID.randomUUID();
		
		event = new Event(
				eventId,
				eventCategory,
				eventType,
				eventTimestamp,
				eventData);		
	}		
	
	@Test
	void findAll_shouldReturnList() {
		
		Mockito.when(userRepository.findAll()).thenReturn(List.of(user));
		Mockito.when(userMapper.fromListDomainToListDto(Mockito.anyList())).thenReturn(List.of(userDto));
		
		List<UserDto> list = authGrpcServiceAdapter.findAll();
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);		
	}

	@Test
	void findAll_withPageable_shouldReturnList() {
		
		Mockito.when(userRepository.findAll(Mockito.any(Pageable.class))).thenReturn(List.of(user));
		Mockito.when(userMapper.fromListDomainToListDto(Mockito.anyList())).thenReturn(List.of(userDto));
		
		List<UserDto> list = authGrpcServiceAdapter.findAll(pageable);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);		
	}

	@Test
	void register_shouldReturnOptUserDto() {
		
		Mockito.when(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
		Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);
		Mockito.when(userAppGrpcClient.createUser(Mockito.any(UserAppDto.class))).thenReturn(true);
		Mockito.doNothing().when(publisherAdapter).publishEvent(Mockito.any(Event.class));
		Mockito.when(userMapper.fromDomToEvent(Mockito.any(User.class))).thenReturn(event);
		Mockito.when(userMapper.fromDomainToDto(Mockito.any(User.class))).thenReturn(userDto);
		
		Optional<UserDto> optResult = authGrpcServiceAdapter.register(registerRequestDto);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().userId()).isEqualTo(userId)
		);
	}
	
}
