package com.github.kat_ka.spend_the_night.model.item;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Street {

	@Schema(nullable = false, example = "B\u00e4nschstra\u00dfe")
	@NotBlank(message = "The accommodation address street name must not be empty")
	private String name;

	@Schema(nullable = false, example = "1")
	@NotBlank(message = "The accommodation address street number must not be empty")
	private String number;
}
