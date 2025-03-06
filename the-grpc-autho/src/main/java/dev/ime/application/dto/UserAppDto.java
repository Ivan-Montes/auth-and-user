package dev.ime.application.dto;

import dev.ime.common.config.GlobalConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserAppDto(
		Long userAppId,
		@NotBlank @Pattern( regexp = GlobalConstants.PATTERN_EMAIL ) String email,
		@NotBlank @Pattern( regexp = GlobalConstants.PATTERN_NAME_FULL ) String name,	
		@NotBlank @Pattern( regexp = GlobalConstants.PATTERN_NAME_FULL ) String lastname	
		) {

}
