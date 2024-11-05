package com.github.kat_ka.spend_the_night.deserialisation;

import com.github.kat_ka.spend_the_night.model.item.Amenity;

public class AmenitiesDeserializer extends EnumSetDeserializer<Amenity> {
	public AmenitiesDeserializer() {
		super(Amenity.class);
	}
}
