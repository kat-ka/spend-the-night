package com.github.kat_ka.spend_the_night.conversion.jpa;

import com.github.kat_ka.spend_the_night.model.item.Language;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LanguagesConverter extends SetConverter<Language> {

	public LanguagesConverter() {
		super(Language.class);
	}
}
