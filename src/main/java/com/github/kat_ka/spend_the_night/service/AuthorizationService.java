package com.github.kat_ka.spend_the_night.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthorizationService {

	protected String getClientId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String clientId = ((Jwt) auth.getCredentials()).getClaimAsString("clientId");
		log.info("for client id {}", clientId);
		return clientId;
	}
}
