package dev.ime.application.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import dev.ime.application.exception.EmptyResponseException;
import dev.ime.application.exception.JwtTokenEmailRestriction;
import dev.ime.application.utils.JwtUtil;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.port.inbound.AuthorizationServicePort;

@Service
public class AuthorizationServiceAdapter implements AuthorizationServicePort {

	private final JwtUtil jwtUtil;
	private static final Logger logger = LoggerFactory.getLogger(AuthorizationServiceAdapter.class);

	public AuthorizationServiceAdapter(JwtUtil jwtUtil) {
		super();
		this.jwtUtil = jwtUtil;
	}

	@Override
	public void checkJwtTokenOwner(String email) {

		String jwtTokenMail = jwtUtil.getSubFromJwt();

		if (!email.equals(jwtTokenMail)) {
			logger.error(GlobalConstants.MSG_PATTERN_SEVERE, GlobalConstants.EX_JWTTOKENEMAILRESTRICTION_DESC, email);
			throw new JwtTokenEmailRestriction(
					Map.of(GlobalConstants.OBJ_FIELD, GlobalConstants.USER_EMAIL, GlobalConstants.OBJ_VALUE, email));
		}
	}

	@Override
	public String getJwtTokenEmail() {

		String jwtTokenMail = jwtUtil.getSubFromJwt();

		if (jwtTokenMail == null || jwtTokenMail.isBlank()) {
			logger.error(GlobalConstants.MSG_PATTERN_SEVERE, GlobalConstants.EX_EMPTYRESPONSE_DESC,
					GlobalConstants.USER_EMAIL);
			throw new EmptyResponseException(Map.of(GlobalConstants.OBJ_FIELD, GlobalConstants.USER_EMAIL));
		}

		return jwtTokenMail;
	}

}
