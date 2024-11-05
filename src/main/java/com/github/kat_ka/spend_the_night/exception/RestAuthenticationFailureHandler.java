package com.github.kat_ka.spend_the_night.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RestAuthenticationFailureHandler implements AuthenticationFailureHandler {

	@Autowired
	private RestProblem restProblem;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException e) throws IOException, ServletException {
		RestProblem.initResponse(response, HttpStatus.UNAUTHORIZED);
		String message = e.getMessage();
		restProblem.writeError(request, response, HttpStatus.UNAUTHORIZED, message);
		log.info("Authentication failure: {}: {}", e.getClass().getName(), message);
	}
}
