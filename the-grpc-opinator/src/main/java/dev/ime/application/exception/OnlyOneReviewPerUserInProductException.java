package dev.ime.application.exception;

import java.util.Map;
import java.util.UUID;

import dev.ime.common.constants.GlobalConstants;

public class OnlyOneReviewPerUserInProductException extends BasicException {

	private static final long serialVersionUID = 5540428183088920472L;

	public OnlyOneReviewPerUserInProductException(Map<String, String> errors) {
		super(
				UUID.randomUUID(),
				GlobalConstants.EX_ONLYONEREV,
				GlobalConstants.EX_ONLYONEREV_DESC,
				errors);
	}

}
