package dev.ime.api.error;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import dev.ime.application.dto.ErrorResponse;
import dev.ime.application.exception.BasicException;
import dev.ime.common.constants.GlobalConstants;

@ControllerAdvice
public class ApiExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

	public ApiExceptionHandler() {
		super();
	}	
	@ExceptionHandler({
		dev.ime.application.exception.BasicException.class
		})	
	public ResponseEntity<ErrorResponse> handleBasicExceptionExtendedClasses(BasicException ex){

		logger.error(GlobalConstants.MSG_PATTERN_SEVERE, GlobalConstants.EX_BASIC, ex.getMessage());
		
		ErrorResponse response = new ErrorResponse(
				ex.getIdentifier(),
				ex.getName(),
				ex.getDescription(), 
				ex.getErrors()
    		);
		   
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}	

	@ExceptionHandler(Exception.class)		
	public ResponseEntity<ErrorResponse> lastExceptionStands(Exception ex){

		logger.error(GlobalConstants.MSG_PATTERN_SEVERE, GlobalConstants.EX_PLAIN, ex.getMessage());
		
		ErrorResponse response = new ErrorResponse(
	            UUID.randomUUID(),
	            GlobalConstants.EX_PLAIN,
	            GlobalConstants.EX_PLAIN_DESC,
	            Map.of(ex.getLocalizedMessage(), ex.getMessage())
	        );
		   
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}	

}
