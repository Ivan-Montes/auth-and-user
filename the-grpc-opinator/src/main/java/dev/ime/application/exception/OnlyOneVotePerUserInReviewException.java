package dev.ime.application.exception;

import java.util.Map;
import java.util.UUID;

import dev.ime.common.constants.GlobalConstants;

public class OnlyOneVotePerUserInReviewException extends BasicException {

	private static final long serialVersionUID = -5473480991302397274L;

	public OnlyOneVotePerUserInReviewException(Map<String, String> errors) {
		super(
				UUID.randomUUID(),
				GlobalConstants.EX_ONLYONEVOTE,
				GlobalConstants.EX_ONLYONEVOTE_DESC,
				errors);
	}

}
