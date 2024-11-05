package com.github.kat_ka.spend_the_night.conversion;

import com.github.kat_ka.spend_the_night.model.data.HostPreferencesEntity;
import com.github.kat_ka.spend_the_night.model.item.HostPreferences;

public final class HostPreferencesConverter {

	private HostPreferencesConverter() {}

	protected static HostPreferencesEntity apiToEntity(HostPreferences hostPreferences) {
		var hostPreferencesEntity = new HostPreferencesEntity();
		updateEntity(hostPreferencesEntity, hostPreferences);
		return hostPreferencesEntity;
	}

	protected static void updateEntity(
			HostPreferencesEntity hostPreferencesEntity, HostPreferences hostPreferences) {
		hostPreferencesEntity.setMaxGuests(hostPreferences.getMaxGuests());
		hostPreferencesEntity.setCheckInTime(hostPreferences.getCheckInTime());
		hostPreferencesEntity.setCheckOutTime(hostPreferences.getCheckOutTime());
		hostPreferencesEntity.setSmokingAllowed(hostPreferences.getSmokingAllowed());
		hostPreferencesEntity.setPetsAllowed(hostPreferences.getPetsAllowed());
		hostPreferencesEntity.setLanguagesSpoken(hostPreferences.getLanguagesSpoken());
	}

	protected static HostPreferences entityToApi(HostPreferencesEntity hostPreferencesEntity) {
		var hostPreferences = new HostPreferences();
		hostPreferences.setMaxGuests(hostPreferencesEntity.getMaxGuests());
		hostPreferences.setCheckInTime(hostPreferencesEntity.getCheckInTime());
		hostPreferences.setCheckOutTime(hostPreferencesEntity.getCheckOutTime());
		hostPreferences.setSmokingAllowed(hostPreferencesEntity.getSmokingAllowed());
		hostPreferences.setPetsAllowed(hostPreferencesEntity.getPetsAllowed());
		hostPreferences.setLanguagesSpoken(hostPreferencesEntity.getLanguagesSpoken());
		return hostPreferences;
	}
}
