package com.github.kat_ka.spend_the_night.model.item;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.kat_ka.spend_the_night.deserialisation.AmenitiesDeserializer;
import com.github.kat_ka.spend_the_night.deserialisation.CurrencyDeserializer;
import com.github.kat_ka.spend_the_night.deserialisation.StrictIntegerDeserializer;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.net.URL;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import lombok.Data;

@Data
public class Accommodation {

	@Schema(hidden = true)
	private UUID id;

	@Schema(nullable = false, example = "Cozy Apartment in City Center")
	@NotBlank(message = "The accommodation title must not be empty")
	private String title;

	@Schema(nullable = true, example = "A comfortable one-bedroom apartment with a beautiful view of the city.")
	private String description;

	@Schema(nullable = false)
	@NotNull(message = "The accommodation address must not be empty")
	@Valid
	private Address address;

	@Schema(type = "array", format = "uri", nullable = true, example = "[\"https://example.com/1.jpg\", \"https://example.com/2.jpg\"]")
	private Set<@NotNull(message = "All accommodation pictures must not be empty") URL> pictures;

	@Schema(nullable = false, minimum = "0", example = "50")
	@NotNull(message = "The accommodation price per night must not be empty")
	@JsonDeserialize(using = StrictIntegerDeserializer.class)
	@Min(value = 0, message = "The accommodation price per night must not be negative")
	private Integer pricePerNight;

	private static final Currency DEFAULT_CURRENCY = Currency.EUR;

	@Schema(defaultValue = "EUR")
	@JsonDeserialize(using = CurrencyDeserializer.class)
	private Currency currency = DEFAULT_CURRENCY;

	@JsonSetter
	public void setCurrency(Currency currency) {
		this.currency = (currency != null) ? currency : DEFAULT_CURRENCY;
	}

	@Schema(hidden = true)
	private Double priceInEuro;

	@Schema(type = "array", format = "date", nullable = false, example = "[\"2025-01-17\", \"2025-01-18\", \"2025-01-19\"]")
	@NotNull(message = "Available dates for the accommodation missing")
	@Size(min = 1, message = "Available dates for the accommodation missing")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Set<@NotNull(message = "All accommodation available dates must not be empty") LocalDate> availableDates;

	@Schema(type = "array", format = "string", nullable = true, example = "[\"WIFI\", \"BBQ_GRILL\"]")
	@JsonDeserialize(using = AmenitiesDeserializer.class)
	private Set<Amenity> amenities;

	@Schema(nullable = false)
	@NotNull(message = "The accommodation host preferences must not be empty")
	@Valid
	private HostPreferences hostPreferences;
}
