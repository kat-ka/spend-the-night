package com.github.kat_ka.spend_the_night.deserialisation;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.github.kat_ka.spend_the_night.exception.OfferValidationProblem;

import java.io.IOException;

public class EnumDeserializer<T extends Enum<T>> extends JsonDeserializer<T> {

	private final Class<T> enumClass;

	public EnumDeserializer(Class<T> enumClass) {
		this.enumClass = enumClass;
	}

	@Override
	public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
		String enumName = p.getText();
		return getEnumConstant(enumName);
	}

	protected T getEnumConstant(String enumName) throws OfferValidationProblem {
		String enumNameUpperCase = enumName.toUpperCase();
		try {
			return Enum.valueOf(enumClass, enumNameUpperCase);
		} catch (IllegalArgumentException e) {
			throw new OfferValidationProblem(
					"Invalid " + enumClass.getSimpleName() + " value: " + enumName);
		}
	}
}
