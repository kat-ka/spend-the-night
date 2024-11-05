package com.github.kat_ka.spend_the_night.model.item;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PlaceOffer {

	@Schema(nullable = false, example = "Tialda Bons")
	@NotBlank(message = "The user name must not be empty")
	private String userName;

	@Schema(nullable = false)
	@NotNull(message = "The accommodation must not be empty")
	@Valid
	private Accommodation accommodation;
}
