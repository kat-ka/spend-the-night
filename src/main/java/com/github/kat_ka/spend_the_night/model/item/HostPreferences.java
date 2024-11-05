package com.github.kat_ka.spend_the_night.model.item;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.kat_ka.spend_the_night.deserialisation.LanguagesDeserializer;
import com.github.kat_ka.spend_the_night.deserialisation.PetsAllowedDeserializer;
import com.github.kat_ka.spend_the_night.deserialisation.SmokingAllowedDeserializer;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;
import java.util.Set;

import lombok.Data;

@Data
public class HostPreferences {

	@Schema(nullable = false, minimum = "1", example = "2")
	@Positive(message = "The accommodation host preferences max guests number must be at least 1")
	private int maxGuests;

	@Schema(type = "string", format = "partial-time", nullable = false, example = "15:00")
	@NotNull(message = "The accommodation host preferences check in time must not be empty")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	private LocalTime checkInTime;

	@Schema(type = "string", format = "partial-time", nullable = false, example = "11:00")
	@NotNull(message = "The accommodation host preferences check out time must not be empty")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	private LocalTime checkOutTime;

	private static final SmokingAllowed DEFAULT_SMOKING_ALLOWED = SmokingAllowed.NO;

	@Schema(defaultValue = "NO")
	@JsonDeserialize(using = SmokingAllowedDeserializer.class)
	private SmokingAllowed smokingAllowed = DEFAULT_SMOKING_ALLOWED;

	@JsonSetter
	public void setSmokingAllowed(SmokingAllowed smokingAllowed) {
		this.smokingAllowed = (smokingAllowed != null) ? smokingAllowed : DEFAULT_SMOKING_ALLOWED;
	}

	private static final PetsAllowed DEFAULT_PETS_ALLOWED = PetsAllowed.NO;

	@Schema(defaultValue = "NO")
	@JsonDeserialize(using = PetsAllowedDeserializer.class)
	private PetsAllowed petsAllowed = DEFAULT_PETS_ALLOWED;

	@JsonSetter
	public void setPetsAllowed(PetsAllowed petsAllowed) {
		this.petsAllowed = (petsAllowed != null) ? petsAllowed : DEFAULT_PETS_ALLOWED;
	}

	@Schema(type = "array", format = "string", nullable = false, example = "[ \"EN\", \"DE\"]")
	@NotNull(message = "Spoken languages for the accommodation host preferences missing")
	@Size(min = 1, message = "Spoken languages for the accommodation host preferences missing")
	@JsonDeserialize(using = LanguagesDeserializer.class)
	private Set<Language> languagesSpoken;
}
