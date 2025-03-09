package dev.ime.api.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessageV3;

import dev.ime.application.exception.ValidationException;
import dev.ime.common.config.GlobalConstants;
import dev.proto.CreateUserRequest;

@Component
public class DtoValidator {

	private static final Map<Class<? extends GeneratedMessageV3>, Function<String, FieldDescriptor>> FIELD_DESCRIPTOR_MAP;
	
	static {
        FIELD_DESCRIPTOR_MAP = new HashMap<>();
        FIELD_DESCRIPTOR_MAP.put(CreateUserRequest.class, CreateUserRequest.getDescriptor()::findFieldByName);
    }
	
	public void validateCreateUserRequest(CreateUserRequest request) {
		
		checkName(request.getName());
		checkStringField(request.getLastname(), GlobalConstants.PATTERN_NAME_FULL);
		checkString(request, GlobalConstants.USER_EMAIL, GlobalConstants.PATTERN_EMAIL);
		checkString(request, GlobalConstants.USER_PASS , GlobalConstants.PATTERN_NAME_FULL);
		
	}
	
	private void checkName(String name) {
		
		Pattern compiledPattern = Pattern.compile(GlobalConstants.PATTERN_NAME_FULL);
	    Matcher matcher = compiledPattern.matcher(name);
	    if (!matcher.matches()) {
	        throw new ValidationException(Map.of(GlobalConstants.USERAPP_NAME, name));
	    }	    
	}

	private void checkStringField(String value, String patternConstraint) {
		
		Pattern compiledPattern = Pattern.compile(patternConstraint);
	    Matcher matcher = compiledPattern.matcher(value);
	    if (!matcher.matches()) {
	        throw new ValidationException(Map.of(GlobalConstants.OBJ_VALUE, value));
	    }	    
	}

	private <T> void checkString(T request, String key, String patternConstraint) {
		
		FieldDescriptor fieldDescriptor = extractFieldDescriptor(request, key);		

		String value = Optional.ofNullable(((GeneratedMessageV3) request).getField(fieldDescriptor))
				.map(Object::toString)
                .orElse("");
		Pattern compiledPattern = Pattern.compile(patternConstraint);
	    Matcher matcher = compiledPattern.matcher(value);
	    if (!matcher.matches()) {
	        throw new ValidationException(Map.of(GlobalConstants.OBJ_FIELD, key));
	    }	    
	}
	
	private <T> FieldDescriptor extractFieldDescriptor(T request, String key) {
        
		return Optional.ofNullable(FIELD_DESCRIPTOR_MAP.get(request.getClass()))
                .map(function -> function.apply(key))
                .orElseThrow(() -> new IllegalArgumentException(GlobalConstants.MSG_UNSUP_REQ + ":" + request.getClass().getSimpleName()));
    }
	
}
