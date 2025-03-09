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
import dev.ime.application.exception.EmptyResponseException;
import dev.ime.application.service.AuthGrpcServiceAdapter;
import dev.ime.common.config.GlobalConstants;
import dev.ime.common.mapper.UserMapper;
import dev.proto.AuthGrpcServiceGrpc;
import dev.proto.CreateUserRequest;
import dev.proto.ListUsersResponse;
import dev.proto.PaginationRequest;
import dev.proto.UserProto;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class AuthGrpcServiceImpl extends AuthGrpcServiceGrpc.AuthGrpcServiceImplBase {

	private final AuthGrpcServiceAdapter authGrpcService;
	private final UserMapper userMapper;
	private final DtoValidator dtoValidator;
	private static final Logger logger = LoggerFactory.getLogger(AuthGrpcServiceImpl.class);

	public AuthGrpcServiceImpl(AuthGrpcServiceAdapter authGrpcService, UserMapper userMapper, DtoValidator dtoValidator) {
		super();
		this.authGrpcService = authGrpcService;
		this.userMapper = userMapper;
		this.dtoValidator = dtoValidator;
	}

	@Override
	public void createUser(CreateUserRequest request, StreamObserver<UserProto> responseObserver) {
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, request.getClass().getSimpleName(), request);

		dtoValidator.validateCreateUserRequest(request);		
		UserProto userProto = Optional.ofNullable(request)
				.map(userMapper::fromRegisterRequestToRegisterDto)
				.flatMap(authGrpcService::register)
				.map(userMapper::fromUserDtoToUserProto)
				.map(item -> {
					logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.USER_CREATED, item);
					return item;
				}).orElseThrow(
						() -> new EmptyResponseException(Map.of(GlobalConstants.CAT_CAT, GlobalConstants.MSG_NODATA)));

		responseObserver.onNext(userProto);
		responseObserver.onCompleted();
	}

	@Override
	public void listUsers(Empty request, StreamObserver<ListUsersResponse> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, request.getClass().getSimpleName(), request);
		
		List<UserProto> userProtoList = userMapper.fromListUserDtoToListUserProto(authGrpcService.findAll());
		ListUsersResponse listUsersResponse = ListUsersResponse.newBuilder().addAllUsers(userProtoList).build();

		responseObserver.onNext(listUsersResponse);
		responseObserver.onCompleted();
	}

	@Override
	public void listUsersPaginated(PaginationRequest request, StreamObserver<ListUsersResponse> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, request.getClass().getSimpleName(), request);
		
		PaginationDto paginationDto = createPaginationDto(request);
		PageRequest pageRequest = createPageable(paginationDto);
		List<UserProto> userProtoList = userMapper.fromListUserDtoToListUserProto(authGrpcService.findAll(pageRequest));
		ListUsersResponse listUsersResponse = ListUsersResponse.newBuilder().addAllUsers(userProtoList)
				.build();

		responseObserver.onNext(listUsersResponse);
		responseObserver.onCompleted();
	}
	
	private PaginationDto createPaginationDto(PaginationRequest request) {
		
		Set<String> attrsSet = Set.of(GlobalConstants.USER_ID,GlobalConstants.USER_EMAIL);
		Integer page = Optional.ofNullable(request.getPage()).filter( i -> i >= 0).orElse(0);
    	Integer size = Optional.ofNullable(request.getSize()).filter( i -> i > 0).orElse(100);
        String sortBy = Optional.ofNullable(request.getSortBy()).filter(attrsSet::contains).orElse(GlobalConstants.USER_ID);
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
