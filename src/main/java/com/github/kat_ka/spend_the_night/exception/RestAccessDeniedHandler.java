package com.github.kat_ka.spend_the_night.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RestAccessDeniedHandler implements AccessDeniedHandler {

	@Autowired
	private RestProblem restProblem;

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)
			throws IOException, ServletException {
		RestProblem.initResponse(response, HttpStatus.FORBIDDEN);
		String message = e.getMessage();
		restProblem.writeError(request, response, HttpStatus.FORBIDDEN, message);
		log.info("No Access: {}: {}", e.getClass().getName(), message);
	}
}
