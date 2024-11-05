package com.github.kat_ka.spend_the_night.conversion;

import com.github.kat_ka.spend_the_night.model.data.AccommodationEntity;
import com.github.kat_ka.spend_the_night.model.item.PlaceSearchResult;

public final class PlaceSearchResultConverter {

	private PlaceSearchResultConverter() {}

	public static PlaceSearchResult entityToApi(AccommodationEntity accommodationEntity) {
		var placeSearchResult = new PlaceSearchResult();
		placeSearchResult.setUser(UserConverter.entityToApi(accommodationEntity.getUser()));
		placeSearchResult.setAccommodation(AccommodationConverter.entityToApi(accommodationEntity));
		return placeSearchResult;
	}
}
