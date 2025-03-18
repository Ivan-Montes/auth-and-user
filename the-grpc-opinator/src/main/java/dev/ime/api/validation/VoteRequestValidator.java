package dev.ime.api.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessageV3;

import dev.ime.application.exception.ValidationException;
import dev.ime.common.config.GlobalConstants;
import dev.proto.CreateVoteRequest;
import dev.proto.DeleteVoteRequest;
import dev.proto.GetVoteRequest;
import dev.proto.UpdateVoteRequest;

@Component
public class VoteRequestValidator {

	private static final Map<Class<? extends GeneratedMessageV3>, Function<String, FieldDescriptor>> FIELD_DESCRIPTOR_MAP;

	static {
        FIELD_DESCRIPTOR_MAP = new HashMap<>();
        FIELD_DESCRIPTOR_MAP.put(CreateVoteRequest.class, CreateVoteRequest.getDescriptor()::findFieldByName);
        FIELD_DESCRIPTOR_MAP.put(UpdateVoteRequest.class, UpdateVoteRequest.getDescriptor()::findFieldByName);
    }

	public void validateCreateRequest(CreateVoteRequest request) {
		
		checkIdField(request.getReviewId(), GlobalConstants.REV_ID);
	}

	public void validateUpdateRequest(UpdateVoteRequest request) {
		
		checkIdField(request.getVoteId(), GlobalConstants.VOT_ID);

	}

	public void validateDeleteRequest(DeleteVoteRequest request) {
		
		checkIdField(request.getVoteId(), GlobalConstants.VOT_ID);
		
	}

	public void validateGetRequest(GetVoteRequest request) {
		
		checkIdField(request.getVoteId(), GlobalConstants.VOT_ID);
		
	}

	private void checkIdField(Long value, String field) {
		
		if ( value == null || value <= 0 ) {
	        throw new ValidationException(Map.of(field, String.valueOf(value)));
	    }	    
	}
	
}
