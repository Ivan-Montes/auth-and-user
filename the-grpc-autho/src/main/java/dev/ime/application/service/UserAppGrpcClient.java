package dev.ime.application.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.devh.boot.grpc.client.inject.GrpcClient;
import dev.proto.CreateUserAppRequest;
import dev.proto.UserAppGrpcServiceGrpc;
import io.grpc.StatusRuntimeException;
import dev.ime.application.dto.UserAppDto;
import dev.ime.application.exception.GrpcClientCommunicationException;
import dev.ime.common.config.GlobalConstants;
import dev.ime.common.mapper.UserMapper;

@Service
public class UserAppGrpcClient {

	@GrpcClient("UserAppGrpcClient")
	private UserAppGrpcServiceGrpc.UserAppGrpcServiceBlockingStub blockingStub;
	private static final Logger logger = LoggerFactory.getLogger(UserAppGrpcClient.class);
	
	public Boolean createUser(UserAppDto userAppDto)  {
		
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.CREATE_USERAPP, userAppDto);

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			UserMapper userMapper = new UserMapper(objectMapper);
			CreateUserAppRequest createUserAppRequest =  userMapper.fromUserAppDtoToCreateUserAppRequest(userAppDto);
			return blockingStub.createUser(createUserAppRequest).getResult();

		} catch (StatusRuntimeException ex) {
			
		    throw new GrpcClientCommunicationException(Map.of(ex.getStatus().getCode().name(),ex.getStatus().getDescription()));

		}		
	}	
}
