package dev.ime.api.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Descriptors.FieldDescriptor;

import dev.ime.application.exception.ValidationException;
import dev.ime.common.constants.GlobalConstants;
import dev.proto.CreateProductRequest;
import dev.proto.DeleteProductRequest;
import dev.proto.GetProductRequest;
import dev.proto.UpdateProductRequest;

@Component
public class ProductRequestValidator {

	private static final Map<Class<? extends GeneratedMessageV3>, Function<String, FieldDescriptor>> FIELD_DESCRIPTOR_MAP;

	static {
        FIELD_DESCRIPTOR_MAP = new HashMap<>();
        FIELD_DESCRIPTOR_MAP.put(CreateProductRequest.class, CreateProductRequest.getDescriptor()::findFieldByName);
        FIELD_DESCRIPTOR_MAP.put(UpdateProductRequest.class, UpdateProductRequest.getDescriptor()::findFieldByName);
        FIELD_DESCRIPTOR_MAP.put(DeleteProductRequest.class, DeleteProductRequest.getDescriptor()::findFieldByName);
        FIELD_DESCRIPTOR_MAP.put(GetProductRequest.class, GetProductRequest.getDescriptor()::findFieldByName);
   }

	public void validateCreateRequest(CreateProductRequest request) {
		
		checkString(request, GlobalConstants.PROD_NAME, GlobalConstants.PATTERN_NAME_FULL);
		checkString(request, GlobalConstants.PROD_DESC, GlobalConstants.PATTERN_DESC_FULL);
		checkIdField(request.getCategoryId(), GlobalConstants.CAT_ID);

	}

	public void validateUpdateRequest(UpdateProductRequest request) {
		
		checkIdField(request.getProductId(), GlobalConstants.PROD_ID);
		checkString(request, GlobalConstants.PROD_NAME, GlobalConstants.PATTERN_NAME_FULL);
		checkString(request, GlobalConstants.PROD_DESC, GlobalConstants.PATTERN_DESC_FULL);
		checkIdField(request.getCategoryId(), GlobalConstants.CAT_ID);
		
	}

	public void validateDeleteRequest(DeleteProductRequest request) {
		
		checkIdField(request.getProductId(), GlobalConstants.PROD_ID);
		
	}

	public void validateGetRequest(GetProductRequest request) {
		
		checkIdField(request.getProductId(), GlobalConstants.PROD_ID);
		
	}

	private void checkIdField(Long value, String field) {
		
		if ( value == null || value <= 0 ) {
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
