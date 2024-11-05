package com.github.kat_ka.spend_the_night.model.data;

import com.github.kat_ka.spend_the_night.conversion.jpa.LanguagesConverter;
import com.github.kat_ka.spend_the_night.model.item.Language;
import com.github.kat_ka.spend_the_night.model.item.PetsAllowed;
import com.github.kat_ka.spend_the_night.model.item.SmokingAllowed;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalTime;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "host_preferences")
@Data
@EqualsAndHashCode(callSuper = false)
public class HostPreferencesEntity extends BaseEntity {

	@Column(name = "max_guests")
	private int maxGuests;

	@Column(name = "check_in_time")
	private LocalTime checkInTime;

	@Column(name = "check_out_time")
	private LocalTime checkOutTime;

	@Enumerated(EnumType.STRING)
	@Column(name = "smoking_allowed")
	private SmokingAllowed smokingAllowed;

	@Enumerated(EnumType.STRING)
	@Column(name = "pets_allowed")
	private PetsAllowed petsAllowed;

	@Convert(converter = LanguagesConverter.class)
	@Column(name = "languages_spoken", columnDefinition = "TEXT")
	private Set<Language> languagesSpoken;

	@OneToOne(mappedBy = "hostPreferences", cascade = CascadeType.ALL, orphanRemoval = true)
	private AccommodationEntity accommodations;
}
