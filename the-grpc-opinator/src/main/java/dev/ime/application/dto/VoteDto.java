package dev.ime.application.dto;

import dev.ime.common.config.GlobalConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record VoteDto(
		Long voteId,
		@NotBlank @Pattern(regexp = GlobalConstants.PATTERN_EMAIL)String email,
		@NotNull @Min(1) Long reviewId,
		@NotNull boolean useful
	    ) {

}
