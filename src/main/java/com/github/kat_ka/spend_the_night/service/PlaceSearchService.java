package com.github.kat_ka.spend_the_night.service;

import com.github.kat_ka.spend_the_night.conversion.PlaceSearchResultConverter;
import com.github.kat_ka.spend_the_night.model.data.AccommodationEntity;
import com.github.kat_ka.spend_the_night.model.data.UserEntity;
import com.github.kat_ka.spend_the_night.model.item.PlaceSearchResult;
import com.github.kat_ka.spend_the_night.repository.AccommodationRepository;
import com.github.kat_ka.spend_the_night.validation.PlaceOfferValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PlaceSearchService {

	@Autowired
	private AccommodationRepository accommodationRepository;

	@Autowired
	private UserService userService;

	public Page<PlaceSearchResult> getPlaceSearchResults(String userName, String town, final Pageable pageable) {
		UserEntity user = userService.validateAndGetAuthorizedUser(userName);
		if (user != null) {
			town = PlaceOfferValidator.validateParamNotBlank("town", town);
			Page<AccommodationEntity> accommodationEntities = accommodationRepository.findByAddress_City(
					town, pageable);
			return accommodationEntities
					.map(accommodationEntity -> {
						PlaceSearchResult result = PlaceSearchResultConverter.entityToApi(
								accommodationEntity);
						result.getUser().setId(null);
						return result;
					});
		}
		return null;
	}
}
