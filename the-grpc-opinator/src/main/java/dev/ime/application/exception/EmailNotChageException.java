package dev.ime.application.exception;

import java.util.Map;
import java.util.UUID;

import dev.ime.common.constants.GlobalConstants;

public class EmailNotChageException extends BasicException {

	private static final long serialVersionUID = -289019841003492252L;

	public EmailNotChageException(Map<String, String> errors) {
		super(
				UUID.randomUUID(), 
				GlobalConstants.EX_EMAILNOTCHANGE, 
				GlobalConstants.EX_EMAILNOTCHANGE_DESC, 
				errors);
	}

}
