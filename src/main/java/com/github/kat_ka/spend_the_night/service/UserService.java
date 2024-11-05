package com.github.kat_ka.spend_the_night.service;

import com.github.kat_ka.spend_the_night.model.data.AccommodationEntity;
import com.github.kat_ka.spend_the_night.model.data.UserEntity;
import com.github.kat_ka.spend_the_night.repository.UserRepository;
import com.github.kat_ka.spend_the_night.validation.PlaceOfferValidator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AuthorizationService authorizationService;

	protected UserEntity validateAndGetAuthorizedUser(String userName) {
		userName = PlaceOfferValidator.validateParamNotBlank("userName", userName);
		return getAuthorizedUser(userName);
	}

	protected UserEntity getAuthorizedUser(final AccommodationEntity accommodationEntity) {
		String userName = accommodationEntity.getUser().getUserName();
		return getAuthorizedUser(userName);
	}

	protected UserEntity getAuthorizedUser(String userName) {
		UserEntity userEntity = null;
		String clientId = authorizationService.getClientId();
		if (StringUtils.isNotBlank(clientId)) {
			userEntity = userRepository
					.findByUserName(userName)
					.orElseThrow(() -> new AccessDeniedException(
							"The token is valid but the user doesn't exist."));
			if (!userEntity.getId().equals(clientId)) {
				throw new AccessDeniedException("The token is valid but the user doesn't match.");
			}
		}
		return userEntity;
	}
}
