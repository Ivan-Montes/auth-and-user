package dev.ime.application.dto;

import dev.ime.common.config.GlobalConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserDto(
		Long userId, 
		@NotBlank @Pattern( regexp = GlobalConstants.PATTERN_EMAIL ) String email,
		@NotBlank @Pattern( regexp = GlobalConstants.PATTERN_NAME_FULL ) String password,
		String userRole
		) {

}
