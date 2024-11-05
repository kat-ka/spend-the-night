package com.github.kat_ka.spend_the_night.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.servlet.http.HttpServletRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import lombok.Getter;

import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Getter
public class ResponseError {

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	private final OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);

	private int status;

	private String error;

	private String message;

	private String path;

	protected ResponseError(HttpStatus status, String message, WebRequest request) {
		setValues(status, message);
		this.path = ((ServletWebRequest) request).getRequest().getRequestURI();
	}

	protected ResponseError(HttpStatus status, String message, HttpServletRequest request) {
		setValues(status, message);
		this.path = request.getRequestURI();
	}

	private void setValues(HttpStatus status, String message) {
		this.status = status.value();
		this.error = status.getReasonPhrase();
		this.message = message;
	}
}
