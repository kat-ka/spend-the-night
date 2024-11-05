package com.github.kat_ka.spend_the_night.model.item.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Data;

@Data
public class OsmLocation {

	@JsonProperty("place_id")
	private long placeId;

	private String licence;

	@JsonProperty("osm_type")
	private String osmType;

	@JsonProperty("osm_id")
	private long osmId;

	private double lat;

	private double lon;

	@JsonProperty("class")
	private String osmClass;

	private String type;

	@JsonProperty("place_rank")
	private long placeRank;

	private double importance;

	private String addresstype;

	private String name;

	@JsonProperty("display_name")
	private String displayName;

	private OsmAddress address;

	private List<String> boundingbox;

	private String svg;
}
