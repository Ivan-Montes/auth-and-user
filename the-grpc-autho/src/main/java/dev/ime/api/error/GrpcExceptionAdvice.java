package dev.ime.api.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ime.application.exception.BasicException;
import dev.ime.common.config.GlobalConstants;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GrpcExceptionAdvice {

	private static final Logger logger = LoggerFactory.getLogger(GrpcExceptionAdvice.class);
    
	public GrpcExceptionAdvice() {
		super();		
	}
	
	@GrpcExceptionHandler
    public StatusException handleBasicExceptionClasses(BasicException ex) {

		logger.error(GlobalConstants.MSG_PATTERN_SEVERE, ex.getMessage(), ex.getErrors());

		Status status = Status.FAILED_PRECONDITION.withDescription(ex.getMessage()).withCause(ex);
        Metadata metadata = new Metadata();
        ex.getErrors().forEach( (k,v) -> metadata.put(Metadata.Key.of(k, Metadata.ASCII_STRING_MARSHALLER),v));
    
        return status.asException(metadata);		
    }
	
	@GrpcExceptionHandler
    public Status handleException(Exception ex) {

		String msg = createExceptionMsg(ex);
		logger.error(GlobalConstants.MSG_PATTERN_SEVERE, "handleException", msg);

		return Status.UNKNOWN.withDescription(msg).withCause(ex);
    }

	private String createExceptionMsg(Exception ex) {		
		 return ex.getLocalizedMessage() != null? ex.getLocalizedMessage():GlobalConstants.EX_PLAIN;
	}
	
}
