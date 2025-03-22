package dev.ime.application.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import dev.ime.common.constants.GlobalConstants;

@Component
public class JwtUtil {

	public String getSubFromJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return ((Jwt) jwtAuth.getPrincipal()).getClaimAsString(GlobalConstants.JWT_USER);
        }
        throw new SecurityException(GlobalConstants.MSG_EMPTYTOKEN);
    }
}
