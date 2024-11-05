package com.github.kat_ka.spend_the_night.service;

import com.github.kat_ka.spend_the_night.conversion.AccommodationConverter;
import com.github.kat_ka.spend_the_night.conversion.PlaceOfferConverter;
import com.github.kat_ka.spend_the_night.model.data.AccommodationEntity;
import com.github.kat_ka.spend_the_night.model.data.PlaceEntity;
import com.github.kat_ka.spend_the_night.model.data.UserEntity;
import com.github.kat_ka.spend_the_night.model.item.PlaceOffer;
import com.github.kat_ka.spend_the_night.repository.AccommodationRepository;
import com.github.kat_ka.spend_the_night.validation.PlaceOfferValidator;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PlaceOfferService {

	@Autowired
	private AccommodationRepository accommodationRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private PlaceOfferValidator placeOfferValidator;

	public PlaceOffer getPlaceOffer(final UUID id) {
		return accommodationRepository
				.findById(id)
				.map(this::convertToPlaceOffer)
				.orElse(null);
	}

	public Page<PlaceOffer> getPlaceOffers(String userName, final Pageable pageable) {
		UserEntity user = userService.validateAndGetAuthorizedUser(userName);
		if (user != null) {
			Page<AccommodationEntity> accommodationEntities = accommodationRepository.findByUser(
					user, pageable);
			return accommodationEntities
					.map(accommodationEntity -> convertToPlaceOffer(user, accommodationEntity));
		}
		return null;
	}

	public PlaceOffer addPlaceOffer(final PlaceOffer placeOffer) {
		placeOfferValidator.validatePlaceOffer(placeOffer);
		String userName = placeOffer.getUserName();
		AccommodationEntity savedAccommodationEntity = add(placeOffer, userName);
		return convertToPlaceOffer(new UserEntity(userName), savedAccommodationEntity);
	}

	public boolean updatePlaceOffer(final UUID id, final PlaceOffer placeOffer) {
		placeOfferValidator.validatePlaceOffer(placeOffer);
		return accommodationRepository
				.findById(id)
				.map(accommodationEntity -> update(accommodationEntity, placeOffer) != null)
				.orElse(false);
	}

	public boolean deletePlaceOffer(final UUID id) {
		return accommodationRepository
				.findById(id)
				.filter(accommodationEntity -> userService.getAuthorizedUser(accommodationEntity) != null)
				.map(accommodationEntity -> {
					accommodationRepository.deleteById(id);
					return true;
				})
				.orElse(false);
	}

	private PlaceOffer convertToPlaceOffer(AccommodationEntity accommodationEntity) {
		UserEntity user = userService.getAuthorizedUser(accommodationEntity);
		return user != null ? convertToPlaceOffer(user, accommodationEntity) : null;
	}

	private PlaceOffer convertToPlaceOffer(UserEntity user, AccommodationEntity accommodationEntity) {
		var placeEntity = new PlaceEntity(user, accommodationEntity);
		return PlaceOfferConverter.entityToApi(placeEntity);
	}

	private AccommodationEntity add(final PlaceOffer placeOffer, String userName) {
		UserEntity user = userService.getAuthorizedUser(userName);
		if (user != null) {
			AccommodationEntity accommodationEntity = AccommodationConverter.apiToEntity(
					placeOffer.getAccommodation());
			accommodationEntity.setId(null);
			accommodationEntity.getAddress().setId(null);
			accommodationEntity.getHostPreferences().setId(null);
			accommodationEntity.setUser(user);
			return accommodationRepository.save(accommodationEntity);
		}
		return null;
	}

	private AccommodationEntity update(
			AccommodationEntity existingAccommodationEntity, final PlaceOffer placeOffer) {
		String userName = placeOffer.getUserName();
		UserEntity user = userService.getAuthorizedUser(userName);
		if (user != null && userName.equals(existingAccommodationEntity.getUser().getUserName())) {
			AccommodationConverter.updateEntity(existingAccommodationEntity, placeOffer.getAccommodation());
			return accommodationRepository.save(existingAccommodationEntity);
		}
		return null;
	}
}
