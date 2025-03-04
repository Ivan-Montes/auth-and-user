package dev.ime.common.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;

import dev.proto.AuthGrpcServiceGrpc;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.security.authentication.BearerAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.check.AccessPredicate;
import net.devh.boot.grpc.server.security.check.AccessPredicateVoter;
import net.devh.boot.grpc.server.security.check.GrpcSecurityMetadataSource;
import net.devh.boot.grpc.server.security.check.ManualGrpcSecurityMetadataSource;
import net.devh.boot.grpc.server.security.interceptors.DefaultAuthenticatingServerInterceptor;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;

@Configuration
public class GrpcConfig {

	@Bean
	GrpcAuthenticationReader grpcAuthenticationReader() {
	    return new BearerAuthenticationReader(BearerTokenAuthenticationToken::new);
	}

	@Bean
	ServerInterceptor grpcAuthInterceptor(AuthenticationManager authenticationManager, GrpcAuthenticationReader authenticationReader) {
	    return new DefaultAuthenticatingServerInterceptor(authenticationManager, authenticationReader);
	}
	
	@Bean
	GrpcServerConfigurer grpcServerConfigurer(ServerInterceptor grpcAuthInterceptor) {
	    return serverBuilder -> serverBuilder.intercept(grpcAuthInterceptor);
	}

	@Bean
	GrpcSecurityMetadataSource grpcSecurityMetadataSource() {
	    final ManualGrpcSecurityMetadataSource source = new ManualGrpcSecurityMetadataSource();
	    source.set(AuthGrpcServiceGrpc.getUpdateUserMethod(), AccessPredicate.authenticated());
	    source.setDefault(AccessPredicate.permitAll());
	    return source;
	}
	
	@SuppressWarnings("deprecation")
	@Bean
	AccessDecisionManager accessDecisionManager() {
	    final List<AccessDecisionVoter<?>> voters = new ArrayList<>();
	    voters.add(new AccessPredicateVoter());
	    return new UnanimousBased(voters);
	}
	
	@Bean
	JwtAuthenticationProvider jwtAuthenticationProvider(JwtDecoder jwtDecoder) {
	    return new JwtAuthenticationProvider(jwtDecoder);
	}

	@Bean
    DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
    }

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration, JwtAuthenticationProvider jwtAuthenticationProvider, DaoAuthenticationProvider daoAuthenticationProvider) {
		return new ProviderManager(jwtAuthenticationProvider, daoAuthenticationProvider);
	}

}
