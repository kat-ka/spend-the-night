package com.github.kat_ka.spend_the_night.deserialisation;

import com.github.kat_ka.spend_the_night.model.item.Language;

public class LanguagesDeserializer extends EnumSetDeserializer<Language> {

	public LanguagesDeserializer() {
		super(Language.class);
	}
}
