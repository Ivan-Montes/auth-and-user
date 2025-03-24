package dev.ime.application.dto;

import dev.ime.common.constants.GlobalConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CategoryDto(
		Long categoryId, 
		@NotBlank @Pattern(regexp = GlobalConstants.PATTERN_NAME_FULL) String categoryName 
		) {

}
