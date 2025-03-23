package dev.ime.application.exception;

import java.util.Map;
import java.util.UUID;

import dev.ime.common.constants.GlobalConstants;

public class GrpcClientCommunicationException extends BasicException{

	private static final long serialVersionUID = -4800491429907807948L;

	public GrpcClientCommunicationException(Map<String, String> errors) {
		super(
				UUID.randomUUID(), 
				GlobalConstants.EX_GRPCCLIENTCOM, 
				GlobalConstants.EX_GRPCCLIENTCOM_DESC, 
				errors);
	}
}
