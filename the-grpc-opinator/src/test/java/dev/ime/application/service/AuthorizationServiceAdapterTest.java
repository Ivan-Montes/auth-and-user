package dev.ime.application.service;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.ime.application.utils.JwtUtil;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceAdapterTest {

	@Mock
	private JwtUtil jwtUtil;
	
	@InjectMocks
	private AuthorizationServiceAdapter authorizationServiceAdapter;
	
	private final String tokenMail = "email@email.tk";
	private final String email = "email@email.tk";
	
	@Test
	void checkJwtTokenOwner_shouldValidateRight() {
		
		Mockito.when(jwtUtil.getSubFromJwt()).thenReturn(tokenMail);
		
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> authorizationServiceAdapter.checkJwtTokenOwner(email));
	}

	@Test
	void getJwtTokenEmail_shouldReturnTokenString() {
		
		Mockito.when(jwtUtil.getSubFromJwt()).thenReturn(tokenMail);

		String result = authorizationServiceAdapter.getJwtTokenEmail();
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(result).isNotBlank(),
				()-> Assertions.assertThat(result).isEqualTo(tokenMail)
				);
	}

}
