package dev.ime.application.exception;

import java.util.Map;
import java.util.UUID;

import dev.ime.common.constants.GlobalConstants;

public class JwtTokenEmailRestriction extends BasicException {

	private static final long serialVersionUID = 2352742199814783121L;

	public JwtTokenEmailRestriction(Map<String, String> errors) {
		super(
				UUID.randomUUID(), 
				GlobalConstants.EX_JWTTOKENEMAILRESTRICTION, 
				GlobalConstants.EX_JWTTOKENEMAILRESTRICTION_DESC, 
				errors);
	}

}
