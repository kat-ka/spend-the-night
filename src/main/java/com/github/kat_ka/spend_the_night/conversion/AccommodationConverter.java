package com.github.kat_ka.spend_the_night.conversion;

import com.github.kat_ka.spend_the_night.model.data.AccommodationEntity;
import com.github.kat_ka.spend_the_night.model.data.AddressEntity;
import com.github.kat_ka.spend_the_night.model.data.HostPreferencesEntity;
import com.github.kat_ka.spend_the_night.model.item.Accommodation;

import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

public final class AccommodationConverter {

	private AccommodationConverter() {}

	public static AccommodationEntity apiToEntity(Accommodation accommodation) {
		AccommodationEntity accommodationEntity = initEntity();
		accommodationEntity.setId(accommodation.getId());
		updateEntity(accommodationEntity, accommodation);
		return accommodationEntity;
	}

	public static void updateEntity(AccommodationEntity accommodationEntity, Accommodation accommodation) {
		accommodationEntity.setTitle(accommodation.getTitle());
		accommodationEntity.setDescription(accommodation.getDescription());
		accommodationEntity.setPricePerNight(accommodation.getPricePerNight());
		accommodationEntity.setCurrency(accommodation.getCurrency());
		accommodationEntity.setPriceInEuro(accommodation.getPriceInEuro());
		accommodationEntity.setPictures(accommodation.getPictures());
		Set<LocalDate> sortedDates = new TreeSet<>(accommodation.getAvailableDates());
		accommodationEntity.setAvailableDates(sortedDates);
		accommodationEntity.setAmenities(accommodation.getAmenities());
		AddressConverter.updateEntity(accommodationEntity.getAddress(), accommodation.getAddress());
		HostPreferencesConverter.updateEntity(
				accommodationEntity.getHostPreferences(), accommodation.getHostPreferences());
	}

	public static Accommodation entityToApi(AccommodationEntity accommodationEntity) {
		var accommodation = new Accommodation();
		accommodation.setId(accommodationEntity.getId());
		accommodation.setTitle(accommodationEntity.getTitle());
		accommodation.setDescription(accommodationEntity.getDescription());
		accommodation.setPricePerNight(accommodationEntity.getPricePerNight());
		accommodation.setCurrency(accommodationEntity.getCurrency());
		accommodation.setPriceInEuro(accommodationEntity.getPriceInEuro());
		accommodation.setPictures(accommodationEntity.getPictures());
		Set<LocalDate> sortedDates = new TreeSet<>(accommodationEntity.getAvailableDates());
		accommodation.setAvailableDates(sortedDates);
		accommodation.setAmenities(accommodationEntity.getAmenities());
		accommodation.setAddress(AddressConverter.entityToApi(accommodationEntity.getAddress()));
		accommodation.setHostPreferences(HostPreferencesConverter.entityToApi(
				accommodationEntity.getHostPreferences()));
		return accommodation;
	}

	private static AccommodationEntity initEntity() {
		var accommodationEntity = new AccommodationEntity();
		accommodationEntity.setAddress(new AddressEntity());
		accommodationEntity.setHostPreferences(new HostPreferencesEntity());
		return accommodationEntity;
	}
}
