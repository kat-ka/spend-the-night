package com.github.kat_ka.spend_the_night.deserialisation;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.github.kat_ka.spend_the_night.exception.OfferValidationProblem;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumSetDeserializer<T extends Enum<T>> extends JsonDeserializer<Set<T>> {

	private final Class<T> enumClass;

	private final EnumDeserializer<T> enumDeserializer;

	public EnumSetDeserializer(Class<T> enumClass) {
		this.enumClass = enumClass;
		this.enumDeserializer = new EnumDeserializer<>(enumClass);
	}

	@Override
	public Set<T> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
		String[] enumNames = getEnumNames(p);
		return getEnumConstants(enumNames);

	}

	private String[] getEnumNames(JsonParser p) throws OfferValidationProblem, IOException {
		try {
			return p.readValueAs(String[].class);
		} catch (InvalidDefinitionException e) {
			throw new OfferValidationProblem(
					"Invalid " + enumClass.getSimpleName() + " values: " + p.readValueAs(String.class));
		}
	}

	private Set<T> getEnumConstants(String[] enumNames) throws OfferValidationProblem {
		return Arrays
					.stream(enumNames)
					.filter(Objects::nonNull)
					.map(enumDeserializer::getEnumConstant)
					.collect(Collectors.toSet());
	}
}
