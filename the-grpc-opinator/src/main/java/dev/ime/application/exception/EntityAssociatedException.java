package dev.ime.application.exception;

import java.util.Map;
import java.util.UUID;

import dev.ime.common.constants.GlobalConstants;

public class EntityAssociatedException extends BasicException {

	private static final long serialVersionUID = 5126117098014672199L;

	public EntityAssociatedException(Map<String, String> errors) {
		super(
				UUID.randomUUID(),
				GlobalConstants.EX_ENTITYASSOCIATED,
				GlobalConstants.EX_ENTITYASSOCIATED_DESC,
				errors);		
	}

}
