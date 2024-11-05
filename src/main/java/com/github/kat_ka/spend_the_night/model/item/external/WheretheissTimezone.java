package com.github.kat_ka.spend_the_night.model.item.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class WheretheissTimezone {

	private String latitude;

	private String longitude;

	@JsonProperty("timezone_id")
	private String timezoneId;

	private double offset;

	@JsonProperty("country_code")
	private String countryCode;

	@JsonProperty("map_url")
	private String mapUrl;
}
