package com.github.kat_ka.spend_the_night.model.data;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.ZoneId;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "address")
@Data
@EqualsAndHashCode(callSuper = false)
public class AddressEntity extends BaseEntity {

	@Embedded
	private StreetEntity street;

	private String city;

	private String country;

	@Column(name = "postal_code")
	private String postalCode;

	@OneToOne(mappedBy = "address", cascade = CascadeType.ALL, orphanRemoval = true)
	private AccommodationEntity accommodations;

	private double latitude;

	private double longitude;

	private ZoneId timezone;
}
