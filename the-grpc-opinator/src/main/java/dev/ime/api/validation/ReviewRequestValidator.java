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
import dev.ime.common.constants.GlobalConstants;
import dev.proto.CreateReviewRequest;
import dev.proto.DeleteReviewRequest;
import dev.proto.GetReviewRequest;
import dev.proto.UpdateReviewRequest;

@Component
public class ReviewRequestValidator {

	private static final Map<Class<? extends GeneratedMessageV3>, Function<String, FieldDescriptor>> FIELD_DESCRIPTOR_MAP;
	
	static {
        FIELD_DESCRIPTOR_MAP = new HashMap<>();
        FIELD_DESCRIPTOR_MAP.put(CreateReviewRequest.class, CreateReviewRequest.getDescriptor()::findFieldByName);
        FIELD_DESCRIPTOR_MAP.put(UpdateReviewRequest.class, UpdateReviewRequest.getDescriptor()::findFieldByName);
        FIELD_DESCRIPTOR_MAP.put(DeleteReviewRequest.class, DeleteReviewRequest.getDescriptor()::findFieldByName);
        FIELD_DESCRIPTOR_MAP.put(GetReviewRequest.class, GetReviewRequest.getDescriptor()::findFieldByName);
    }
	
	public void validateCreateRequest(CreateReviewRequest request) {
		
		checkIdField(request.getProductId(), GlobalConstants.PROD_ID);
		checkString(request, GlobalConstants.REV_TXT, GlobalConstants.PATTERN_DESC_FULL);
		checkRating(request.getRating(), GlobalConstants.REV_RAT);
	}

	public void validateUpdateRequest(UpdateReviewRequest request) {
		
		checkIdField(request.getReviewId(), GlobalConstants.REV_ID);
		checkString(request, GlobalConstants.REV_TXT, GlobalConstants.PATTERN_DESC_FULL);
		checkRating(request.getRating(), GlobalConstants.REV_RAT);
		
	}

	public void validateDeleteRequest(DeleteReviewRequest request) {
		
		checkIdField(request.getReviewId(), GlobalConstants.REV_ID);
		
	}

	public void validateGetRequest(GetReviewRequest request) {
		
		checkIdField(request.getReviewId(), GlobalConstants.REV_ID);
		
	}

	private void checkIdField(Long value, String field) {
		
		if ( value == null || value <= 0 ) {
	        throw new ValidationException(Map.of(field, String.valueOf(value)));
	    }	    
	}

	private void checkRating(Integer value, String field) {
		
		if ( value == null || value < 0 || value > 5 ) {
	        throw new ValidationException(Map.of(field, String.valueOf(value)));
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
