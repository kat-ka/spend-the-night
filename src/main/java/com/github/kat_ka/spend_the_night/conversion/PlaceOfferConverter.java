package com.github.kat_ka.spend_the_night.conversion;

import com.github.kat_ka.spend_the_night.model.data.PlaceEntity;
import com.github.kat_ka.spend_the_night.model.data.UserEntity;
import com.github.kat_ka.spend_the_night.model.item.PlaceOffer;

public final class PlaceOfferConverter {

	private PlaceOfferConverter() {}

	public static PlaceEntity apiToEntity(PlaceOffer placeOffer) {
		var placeEntity = new PlaceEntity();
		var userEntity = new UserEntity(placeOffer.getUserName());
		placeEntity.setUser(userEntity);
		placeEntity.setAccommodation(AccommodationConverter.apiToEntity(placeOffer.getAccommodation()));
		return placeEntity;
	}

	public static PlaceOffer entityToApi(PlaceEntity placeEntity) {
		var placeOffer = new PlaceOffer();
		placeOffer.setUserName(placeEntity.getUser().getUserName());
		placeOffer.setAccommodation(AccommodationConverter.entityToApi(placeEntity.getAccommodation()));
		return placeOffer;
	}
}
