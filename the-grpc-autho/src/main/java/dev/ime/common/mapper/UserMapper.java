package dev.ime.common.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.ime.application.dto.RegisterRequestDto;
import dev.ime.application.dto.UserAppDto;
import dev.ime.application.dto.UserDto;
import dev.ime.common.config.GlobalConstants;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.User;
import dev.ime.infrastructure.entity.UserJpaEntity;
import dev.proto.CreateUserAppRequest;
import dev.proto.CreateUserRequest;
import dev.proto.UserProto;

@Component
public class UserMapper {

	private final ObjectMapper objectMapper;
	
	public UserMapper(ObjectMapper objectMapper) {
		super();
		this.objectMapper = objectMapper;
	}

	public RegisterRequestDto fromRegisterRequestToRegisterDto(CreateUserRequest request){
		
		return new RegisterRequestDto(
				request.getName(),
				request.getLastname(),
				request.getEmail(),
				request.getPassword()
				);		
	}
	
	public CreateUserAppRequest fromUserAppDtoToCreateUserAppRequest(UserAppDto userAppDto) {
		
		return CreateUserAppRequest.newBuilder()
				.setEmail(userAppDto.email())
				.setName(userAppDto.name())
				.setLastname(userAppDto.lastname())
				.build();		
	}

	public UserJpaEntity fromDomainToJpa(User user) {
		
		return new UserJpaEntity(
				user.getUserId(),
				user.getEmail(),
				user.getPassword(),
				user.getRole()
				);
	}	
	
	public User fromJpaToDomain(UserJpaEntity entity) {
		
		return new User(
				entity.getUserId(),
				entity.getEmail(),
				entity.getPassword(),
				entity.getRole()
				);
	}
	
	public List<User> fromListJpaToListDomain(List<UserJpaEntity> list) {
		
		if ( list == null ) {
			return new ArrayList<>();
		}
		
		return list.stream()
				.map(this::fromJpaToDomain)
				.toList();	
	}

	public UserDto fromDomainToDto(User user) {
		
		return new UserDto(
				user.getUserId(),
				user.getEmail(),
				user.getPassword(),
				user.getRole().name()
				);
	}

	public List<UserDto> fromListDomainToListDto(List<User> list) {

		if (list == null) {
			return new ArrayList<>();
		}

		return list.stream().map(this::fromDomainToDto).toList();
	}

	public UserProto fromUserDtoToUserProto(UserDto userDto) {
		
		return UserProto.newBuilder()
				.setId(userDto.userId())
				.setEmail(userDto.email())
				.setPassword(userDto.password())
				.setRole(userDto.userRole())
				.build();		
	}

	public List<UserProto> fromListUserDtoToListUserProto(List<UserDto> list) {

		if (list == null) {
			return new ArrayList<>();
		}

		return list.stream().map(this::fromUserDtoToUserProto).toList();

	}

	public Event fromDomToEvent(User dom) {		
		
		return new Event(
				GlobalConstants.USER_CAT,
				GlobalConstants.USER_CREATED,
				createEventData(dom)
				);
		
	}
	
	private Map<String, Object> createEventData(User dom) {
		
		return objectMapper.convertValue(dom, new TypeReference<Map<String, Object>>() {});

	}
}
