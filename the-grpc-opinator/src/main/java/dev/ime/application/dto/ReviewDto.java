package dev.ime.application.dto;

import dev.ime.common.constants.GlobalConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ReviewDto(
		Long reviewId,
		@NotBlank @Pattern(regexp = GlobalConstants.PATTERN_EMAIL) String email,
		@NotNull @Min(1) Long productId,
		@NotBlank @Pattern(regexp = GlobalConstants.PATTERN_DESC_FULL) String reviewText,
		@NotNull @Min(0) int rating
	    ) {

}
