package dev.ime.api.endpoint;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.google.protobuf.Empty;

import dev.ime.api.validation.DtoValidator;
import dev.ime.application.dto.PaginationDto;
import dev.ime.application.dto.UserAppDto;
import dev.ime.application.exception.EmptyResponseException;
import dev.ime.common.config.GlobalConstants;
import dev.ime.common.mapper.UserAppMapper;
import dev.ime.domain.port.inbound.UserAppServicePort;
import dev.proto.CreateUserAppRequest;
import dev.proto.ListUsersAppResponse;
import dev.proto.PaginationRequest;
import dev.proto.UpdateUserAppRequest;
import dev.proto.UserAppCreatedResponse;
import dev.proto.UserAppGrpcServiceGrpc;
import dev.proto.UserAppProto;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class UserAppGrpcServiceImpl extends UserAppGrpcServiceGrpc.UserAppGrpcServiceImplBase {

	private final UserAppServicePort<UserAppDto> userAppServiceAdapter;
	private final UserAppMapper userAppMapper;
	private final DtoValidator dtoValidator;
	private static final Logger logger = LoggerFactory.getLogger(UserAppGrpcServiceImpl.class);

	public UserAppGrpcServiceImpl(UserAppServicePort<UserAppDto> userAppServiceAdapter,
			UserAppMapper userAppMapper, DtoValidator dtoValidator) {
		super();
		this.userAppServiceAdapter = userAppServiceAdapter;
		this.userAppMapper = userAppMapper;
		this.dtoValidator = dtoValidator;
	}

	@Override
	public void createUser(CreateUserAppRequest request, StreamObserver<UserAppCreatedResponse> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, request.getClass().getSimpleName(), request);

		dtoValidator.validateCreateUserAppRequest(request);
		UserAppCreatedResponse userCreatedResponse = Optional.ofNullable(request)
				.map(userAppMapper::fromCreateUserAppRequestToUserAppDto)
				.map(userAppServiceAdapter::create)
				.map(b -> UserAppCreatedResponse.newBuilder().setResult(b).build())
				.map(item -> {
					logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.USERAPP_CREATED, item);
					return item;
				}).orElseThrow(
						() -> new EmptyResponseException(Map.of(GlobalConstants.CAT_CAT, GlobalConstants.MSG_NODATA)));

		responseObserver.onNext(userCreatedResponse);
		responseObserver.onCompleted();
	}

	@Override
	public void updateUser(UpdateUserAppRequest request, StreamObserver<UserAppProto> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, request.getClass().getSimpleName(), request);

		dtoValidator.validateUpdateUserAppRequest(request);
		UserAppDto userAppDto = userAppMapper.fromUpdateUserAppRequestToUserAppDto(request);
		Optional<UserAppDto> userAppDtoSaved = userAppServiceAdapter.update(userAppDto);
		UserAppProto userAppProto = userAppDtoSaved.map(userAppMapper::fromUserAppDtoToUserAppProto).orElseThrow(
				() -> new EmptyResponseException(Map.of(GlobalConstants.USERAPP_CAT, String.valueOf(userAppDto))));

		responseObserver.onNext(userAppProto);
		responseObserver.onCompleted();

	}

	@Override
	public void listUsers(Empty request, StreamObserver<ListUsersAppResponse> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, request.getClass().getSimpleName(), request);
		
		List<UserAppProto> userAppProtoList = userAppMapper
				.fromListUserAppDtoToListUserAppProto(userAppServiceAdapter.findAll());
		ListUsersAppResponse listUsersAppResponse = ListUsersAppResponse.newBuilder().addAllUsers(userAppProtoList)
				.build();

		responseObserver.onNext(listUsersAppResponse);
		responseObserver.onCompleted();
	}

	@Override
	public void listUsersPaginated(PaginationRequest request, StreamObserver<ListUsersAppResponse> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, request.getClass().getSimpleName(), request);
		
		PaginationDto paginationDto = createPaginationDto(request);
		PageRequest pageRequest = createPageable(paginationDto);
		List<UserAppProto> userAppProtoList = userAppMapper
				.fromListUserAppDtoToListUserAppProto(userAppServiceAdapter.findAll(pageRequest));
		ListUsersAppResponse listUsersAppResponse = ListUsersAppResponse.newBuilder().addAllUsers(userAppProtoList)
				.build();

		responseObserver.onNext(listUsersAppResponse);
		responseObserver.onCompleted();
	}
	
	private PaginationDto createPaginationDto(PaginationRequest request) {
		
		Set<String> attrsSet = Set.of(GlobalConstants.USERAPP_ID,GlobalConstants.USERAPP_EMAIL,GlobalConstants.USERAPP_NAME, GlobalConstants.USERAPP_LASTNAME);
		Integer page = Optional.ofNullable(request.getPage()).filter( i -> i >= 0).orElse(0);
    	Integer size = Optional.ofNullable(request.getSize()).filter( i -> i > 0).orElse(100);
        String sortBy = Optional.ofNullable(request.getSortBy()).filter(attrsSet::contains).orElse(GlobalConstants.USERAPP_ID);
        String sortDir = Optional.ofNullable(request.getSortDir())
        		.map(String::toUpperCase)
        		.filter( sorting -> sorting.equals(GlobalConstants.PS_A) || sorting.equals(GlobalConstants.PS_D))
        		.orElse(GlobalConstants.PS_A);
        
        return new PaginationDto(page, size, sortBy, sortDir);
	}
	
	private PageRequest createPageable(PaginationDto paginationDto) {

		Sort sort = Sort.by(Sort.Direction.fromString(paginationDto.sortDir()), paginationDto.sortBy());
		return PageRequest.of(paginationDto.page(), paginationDto.size(), sort);
	}

}
