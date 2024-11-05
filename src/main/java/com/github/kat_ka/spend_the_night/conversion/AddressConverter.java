package com.github.kat_ka.spend_the_night.conversion;

import com.github.kat_ka.spend_the_night.model.data.AddressEntity;
import com.github.kat_ka.spend_the_night.model.item.Address;

public final class AddressConverter {

	private AddressConverter() {}

	protected static AddressEntity apiToEntity(Address address) {
		var addressEntity = new AddressEntity();
		updateEntity(addressEntity, address);
		return addressEntity;
	}

	protected static void updateEntity(AddressEntity addressEntity, Address address) {
		addressEntity.setCity(address.getCity());
		addressEntity.setCountry(address.getCountry());
		addressEntity.setPostalCode(address.getPostalCode());
		addressEntity.setLatitude(address.getLatitude());
		addressEntity.setLongitude(address.getLongitude());
		addressEntity.setTimezone(address.getTimezone());
		addressEntity.setStreet(StreetConverter.apiToEntity(address.getStreet()));
	}

	protected static Address entityToApi(AddressEntity addressEntity) {
		var address = new Address();
		address.setCity(addressEntity.getCity());
		address.setCountry(addressEntity.getCountry());
		address.setPostalCode(addressEntity.getPostalCode());
		setLatitudeLongitudeAndTimezone(address, addressEntity);
		address.setStreet(StreetConverter.entityToApi(addressEntity.getStreet()));
		return address;
	}

	public static void setLatitudeLongitudeAndTimezone(Address address, AddressEntity addressEntity) {
		address.setLatitude(addressEntity.getLatitude());
		address.setLongitude(addressEntity.getLongitude());
		address.setTimezone(addressEntity.getTimezone());
	}
}
