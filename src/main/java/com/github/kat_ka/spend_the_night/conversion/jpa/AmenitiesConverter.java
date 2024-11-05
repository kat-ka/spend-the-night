package com.github.kat_ka.spend_the_night.conversion.jpa;

import com.github.kat_ka.spend_the_night.model.item.Amenity;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AmenitiesConverter extends SetConverter<Amenity> {

	public AmenitiesConverter() {
		super(Amenity.class);
	}
}
