package com.github.kat_ka.spend_the_night.conversion.jpa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

@Converter
public class SetConverter<T> implements AttributeConverter<Set<T>, String> {

	@Autowired
	private ObjectMapper objectMapper;

	private final Class<T> type;

	public SetConverter(Class<T> type) {
		this.type = type;
	}

	@Override
	public String convertToDatabaseColumn(final Set<T> attribute) {
		try {
			return objectMapper.writeValueAsString(attribute);
		} catch (JsonProcessingException e) {
			String message = "Error converting Set of " + type.getSimpleName() + " to JSON";
			throw new IllegalArgumentException(message, e);
		}
	}

	@Override
	public Set<T> convertToEntityAttribute(String dbData) {
		try {
			return objectMapper.readValue(
					dbData, objectMapper.getTypeFactory().constructCollectionType(Set.class, type));
		} catch (JsonProcessingException e) {
			String message = "Error converting JSON to Set of " + type.getSimpleName();
			throw new IllegalArgumentException(message, e);
		}
	}
}
