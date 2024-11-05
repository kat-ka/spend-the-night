package com.github.kat_ka.spend_the_night.deserialisation;

import com.github.kat_ka.spend_the_night.model.item.PetsAllowed;

public class PetsAllowedDeserializer extends EnumDeserializer<PetsAllowed> {

	public PetsAllowedDeserializer() {
		super(PetsAllowed.class);
	}
}
