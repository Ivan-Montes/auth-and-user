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

import dev.ime.application.dto.UserAppDto;
import dev.ime.application.utils.JwtUtil;
import dev.ime.common.config.GlobalConstants;
import dev.ime.common.mapper.UserAppMapper;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.UserApp;
import dev.ime.domain.port.outbound.PublisherPort;
import dev.ime.domain.port.outbound.UserAppRepositoryPort;

@ExtendWith(MockitoExtension.class)
class UserAppServiceAdapterTest {

	@Mock
	private UserAppRepositoryPort userAppRepository;
	@Mock
	private UserAppMapper userAppMapper;
	@Mock
	private JwtUtil jwtUtil;
	@Mock
	private PublisherPort publisherAdapter;

	@InjectMocks
	private UserAppServiceAdapter userAppServiceAdapter;	

	private UserApp userApp;
	private UserAppDto userAppDto;
	private Event event;
	
	private final Long userAppId = 1L;
	private final String email = "email@email.tk";
	private final String name = "name";
	private final String lastname = "lastname";
	private final Pageable pageable = PageRequest.of(0, 10, Sort.by("email").ascending());
	
	private UUID eventId;
	private final String eventCategory = GlobalConstants.USERAPP_CAT;
	private final String eventType = GlobalConstants.USERAPP_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private final Map<String, Object> eventData = new HashMap<>();
	
	@BeforeEach
	private void setUp() {
		
		userApp = new UserApp(userAppId,email,name,lastname);
		userAppDto = new UserAppDto(userAppId,email,name,lastname);

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
		
		Mockito.when(userAppRepository.findAll()).thenReturn(List.of(userApp));
		Mockito.when(userAppMapper.fromListDomainToListDto(Mockito.anyList())).thenReturn(List.of(userAppDto));
		
		List<UserAppDto> list = userAppServiceAdapter.findAll();
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);		
	}

	@Test
	void findAll_withPageable_shouldReturnList() {
		
		Mockito.when(userAppRepository.findAll(Mockito.any(Pageable.class))).thenReturn(List.of(userApp));
		Mockito.when(userAppMapper.fromListDomainToListDto(Mockito.anyList())).thenReturn(List.of(userAppDto));
		
		List<UserAppDto> list = userAppServiceAdapter.findAll(pageable);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);		
	}

	@Test
	void create_shouldReturnTrue() {
		
		Mockito.when(userAppRepository.countByEmail(Mockito.anyString())).thenReturn(0);
		Mockito.when(userAppMapper.fromDtoToDomain(Mockito.any())).thenReturn(userApp);
		Mockito.when(userAppRepository.save(Mockito.any(UserApp.class))).thenReturn(Optional.of(userApp));
		Mockito.when(userAppMapper.fromDomToEvent(Mockito.anyString(),Mockito.any(UserApp.class))).thenReturn(event);
		Mockito.doNothing().when(publisherAdapter).publishEvent(Mockito.any(Event.class));

		boolean result = userAppServiceAdapter.create(userAppDto);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(result).isTrue()
		);		
	}

	@Test
	void update_shouldReturnOptDto() {
		
		Mockito.when(userAppRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(userApp));
		Mockito.when(jwtUtil.getSubFromJwt()).thenReturn(email);
		Mockito.when(userAppMapper.fromDtoToDomain(Mockito.any())).thenReturn(userApp);
		Mockito.when(userAppRepository.save(Mockito.any(UserApp.class))).thenReturn(Optional.of(userApp));
		Mockito.when(userAppMapper.fromDomToEvent(Mockito.anyString(),Mockito.any(UserApp.class))).thenReturn(event);
		Mockito.doNothing().when(publisherAdapter).publishEvent(Mockito.any(Event.class));
		Mockito.when(userAppMapper.fromDomainToDto(Mockito.any(UserApp.class))).thenReturn(userAppDto);
		
		Optional<UserAppDto> optResult =  userAppServiceAdapter.update(userAppDto);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().userAppId()).isEqualTo(userAppId)
		);
	}
}
