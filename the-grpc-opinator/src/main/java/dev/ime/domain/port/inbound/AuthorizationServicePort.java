package dev.ime.domain.port.inbound;

public interface AuthorizationServicePort {

	void checkJwtTokenOwner(String email);
	String getJwtTokenEmail();
}
