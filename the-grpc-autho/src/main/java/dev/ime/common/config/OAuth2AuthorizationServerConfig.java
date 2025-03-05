package dev.ime.common.config;

import java.time.Duration;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

@Configuration
public class OAuth2AuthorizationServerConfig {
	
	@Bean
	RegisteredClientRepository clientRegistrationRepository(PasswordEncoder passwordEncoder, Oauth2Properties oauth2Properties) {
		
		RegisteredClient oidcClient =  RegisteredClient.withId(UUID.randomUUID().toString())
			    .clientId(oauth2Properties.getClientId())
			    .clientSecret(passwordEncoder.encode(oauth2Properties.getClientSecret()))
			    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
			    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
			    .redirectUri(oauth2Properties.getRedirectUri())
			    .scope(OidcScopes.OPENID)
			    .clientSettings(ClientSettings.builder()
			        .requireAuthorizationConsent(true)
			        .build())
			    .tokenSettings(TokenSettings.builder()
			        .accessTokenTimeToLive(Duration.ofMinutes(30))
			        .refreshTokenTimeToLive(Duration.ofDays(30))
			        .build())
			    .build();
        
		return new InMemoryRegisteredClientRepository(oidcClient);
    }
    
}
