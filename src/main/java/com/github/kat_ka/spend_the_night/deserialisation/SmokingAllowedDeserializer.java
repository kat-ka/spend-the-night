package com.github.kat_ka.spend_the_night.deserialisation;

import com.github.kat_ka.spend_the_night.model.item.SmokingAllowed;

public class SmokingAllowedDeserializer extends EnumDeserializer<SmokingAllowed> {

	public SmokingAllowedDeserializer() {
		super(SmokingAllowed.class);
	}
}
