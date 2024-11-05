package com.github.kat_ka.spend_the_night.model.item.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OsmAddress {

	private String road;

	private String neighbourhood;

	private String suburb;

	private String borough;

	private String city;

	@JsonProperty("ISO3166-2-lvl4")
	private String iso3166_2_lvl4;

	private String postcode;

	private String country;

	@JsonProperty("country_code")
	private String countryCode;
}
