package dev.ime.application.dto;

import dev.ime.common.config.GlobalConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ProductDto(
	    Long productId,
	    @NotBlank @Pattern(regexp = GlobalConstants.PATTERN_NAME_FULL) String productName,
	    @NotBlank @Pattern(regexp = GlobalConstants.PATTERN_DESC_FULL) String productDescription,
	    @NotNull @Min(1) Long categoryId
		) {

}
