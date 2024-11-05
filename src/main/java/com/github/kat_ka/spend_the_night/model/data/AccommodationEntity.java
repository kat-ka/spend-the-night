package com.github.kat_ka.spend_the_night.model.data;

import com.github.kat_ka.spend_the_night.conversion.jpa.AmenitiesConverter;
import com.github.kat_ka.spend_the_night.model.item.Amenity;
import com.github.kat_ka.spend_the_night.model.item.Currency;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.net.URL;
import java.time.LocalDate;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.apache.commons.collections4.CollectionUtils;

@Entity
@Table(name = "accommodation", uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "title" }) })
@Data
@EqualsAndHashCode(callSuper = false)
public class AccommodationEntity extends BaseEntity {

	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
	private UserEntity user;

	private String title;

	private String description;

	@Column(name = "price_per_night")
	private int pricePerNight;

	@Enumerated(EnumType.STRING)
	private Currency currency;

	@Column(name = "price_in_euro")
	private double priceInEuro;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "address_id", referencedColumnName = "id", nullable = false)
	private AddressEntity address;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "accommodation_pictures", joinColumns = @JoinColumn(name = "accommodation_id"))
	@Column(name = "picture")
	private Set<URL> pictures;

	public Set<URL> getPictures() {
		return CollectionUtils.isEmpty(pictures) ? null : pictures;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "accommodation_available_dates", joinColumns = @JoinColumn(name = "accommodation_id"))
	@Column(name = "available_date")
	private Set<LocalDate> availableDates;

	@Convert(converter = AmenitiesConverter.class)
	@Column(columnDefinition = "TEXT")
	private Set<Amenity> amenities;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "host_preferences_id", referencedColumnName = "id", nullable = false)
	private HostPreferencesEntity hostPreferences;
}
