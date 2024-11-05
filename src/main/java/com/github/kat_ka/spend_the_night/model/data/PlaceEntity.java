package com.github.kat_ka.spend_the_night.model.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceEntity {

	private UserEntity user;

	private AccommodationEntity accommodation;
}
