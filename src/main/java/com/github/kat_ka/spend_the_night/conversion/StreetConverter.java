package com.github.kat_ka.spend_the_night.conversion;

import com.github.kat_ka.spend_the_night.model.data.StreetEntity;
import com.github.kat_ka.spend_the_night.model.item.Street;

public final class StreetConverter {

	private StreetConverter() {}

	protected static StreetEntity apiToEntity(Street street) {
		return StreetEntity
					.builder()
					.name(street.getName())
					.number(street.getNumber())
					.build();
	}

	protected static Street entityToApi(StreetEntity streetEntity) {
		return Street
					.builder()
					.name(streetEntity.getName())
					.number(streetEntity.getNumber())
					.build();
	}
}
