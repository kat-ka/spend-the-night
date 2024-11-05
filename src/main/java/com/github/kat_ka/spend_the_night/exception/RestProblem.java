package com.github.kat_ka.spend_the_night.exception;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class RestProblem {
	
	@Autowired
	private ObjectMapper objectMapper;
	
	protected static void initResponse(HttpServletResponse response, HttpStatus status) {
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(status.value());
	}
	
	protected void writeError(
			HttpServletRequest request, HttpServletResponse response, HttpStatus status, String message)
					throws IOException, StreamWriteException, DatabindException {
		var error = new ResponseError(status, message, request);
		objectMapper.writeValue(response.getOutputStream(), error);
	}

}
