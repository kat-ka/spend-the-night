package com.github.kat_ka.spend_the_night.deserialisation;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.github.kat_ka.spend_the_night.exception.OfferValidationProblem;

import java.io.IOException;
import java.util.regex.Pattern;

public class StrictIntegerDeserializer extends JsonDeserializer<Integer> {
	
	private static final Pattern NUMBER_PATTERN = Pattern.compile("\\-?\\d+");

	@Override
	public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
		String value = p.getText();
		return noRoundedDoubleValue(value, p);

	}

	private static Integer noRoundedDoubleValue(String value, JsonParser p)
			throws OfferValidationProblem, IOException {
		if (!NUMBER_PATTERN.matcher(value).matches()) {
			throw new OfferValidationProblem(
					"Value '" + value + "' for " + p.currentName() + " is not an Integer");
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new OfferValidationProblem(
					"Value '" + value + "' for " + p.currentName() + " is not an Integer");
		}
	}
}
