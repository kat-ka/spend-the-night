package com.github.kat_ka.spend_the_night.conversion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JsonConverter {

	@Autowired
	private ObjectMapper objectMapper;

	public String toJson(final Object object) {
		if (object != null) {
			try {
				return objectMapper.writeValueAsString(object);
			} catch (JsonProcessingException e) {
				log.warn("JSON processing problem while serializing: {}", e.getMessage());
			}
		}
		return "";
	}

	public String toPrettyJson(final Object object) {
		if (object != null) {
			try {
				return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
			} catch (JsonProcessingException e) {
				log.warn("JSON processing problem while serializing with pretty printer: {}",
						e.getMessage());
			}
		}
		return "";
	}

	public <T> T fromJson(String json, Class<T> returnType) {
		if (json != null && returnType != null) {
			try {
				return objectMapper.readValue(json, returnType);
			} catch (JsonMappingException e) {
				log.warn("JSON mapping problem while deserializing: {}", e.getMessage());
			} catch (JsonProcessingException e) {
				log.warn("JSON processing problem while deserializing: {}", e.getMessage());
			}
		}
		return null;
	}
}
