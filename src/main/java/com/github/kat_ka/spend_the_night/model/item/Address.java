package com.github.kat_ka.spend_the_night.model.item;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.ZoneId;

import lombok.Data;

@Data
public class Address {

	@Schema(nullable = false)
	@NotNull(message = "The accommodation address street must not be empty")
	@Valid
	private Street street;

	@Schema(nullable = false, example = "Berlin")
	@NotBlank(message = "The accommodation address city must not be empty")
	private String city;

	@Schema(nullable = false, example = "10247")
	@NotBlank(message = "The accommodation address postal code must not be empty")
	private String postalCode;

	@Schema(nullable = false, example = "Deutschland")
	@NotBlank(message = "The accommodation address country must not be empty")
	private String country;

	@Schema(hidden = true)
	private Double latitude;

	@Schema(hidden = true)
	private Double longitude;

	@Schema(hidden = true)
	private ZoneId timezone;
}
