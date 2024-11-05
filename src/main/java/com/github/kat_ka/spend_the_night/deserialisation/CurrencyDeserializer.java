package com.github.kat_ka.spend_the_night.deserialisation;

import com.github.kat_ka.spend_the_night.model.item.Currency;

public class CurrencyDeserializer extends EnumDeserializer<Currency> {

	public CurrencyDeserializer() {
		super(Currency.class);
	}
}
