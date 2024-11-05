package com.github.kat_ka.spend_the_night.normalization;

import com.github.kat_ka.spend_the_night.model.item.Accommodation;
import com.github.kat_ka.spend_the_night.model.item.Address;
import com.github.kat_ka.spend_the_night.model.item.PlaceOffer;
import com.github.kat_ka.spend_the_night.model.item.Street;

import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public final class PlaceOfferNormalizer {

	private PlaceOfferNormalizer() {
	}

	public static void normalizePlaceOffer(PlaceOffer placeOffer) {
		placeOffer.setUserName(normalizeStringWithDefault(placeOffer.getUserName(), ""));
		normalizeAccommodation(placeOffer.getAccommodation());
	}

	private static String normalizeStringWithDefault(String s, String defaultValue) {
		return (StringUtils.isBlank(s)) ? defaultValue : s.trim();
	}

	private static void normalizeAccommodation(Accommodation accommodation) {
		accommodation.setTitle(accommodation.getTitle().trim());
		accommodation.setDescription(normalizeStringWithDefault(accommodation.getDescription(), null));
		normalizeAddress(accommodation.getAddress());
		accommodation.setPictures(normalizeSetWithDefault(accommodation.getPictures(), null));
		accommodation.setAmenities(normalizeSetWithDefault(accommodation.getAmenities(), null));
	}

	private static void normalizeAddress(Address address) {
		normalizeStreet(address.getStreet());
		address.setCity(address.getCity().trim());
		address.setPostalCode(address.getPostalCode().trim());
		address.setCountry(address.getCountry().trim());
	}

	private static void normalizeStreet(Street street) {
		street.setName(street.getName().trim());
		street.setNumber(street.getNumber().trim());
	}

	private static <T> Set<T> normalizeSetWithDefault(Set<T> set, Set<T> defaultSet) {
		return CollectionUtils.isEmpty(set) ? defaultSet : set;
	}
}
