package com.github.kat_ka.spend_the_night.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler {

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ResponseError> handleHttpMessageNotReadable(
			HttpMessageNotReadableException ex, WebRequest request) {
		String message = generateHttpMessageNotReadableExceptionMessage(ex);
		log.info("Invalid client request (http message not readable): {}", message);
		return responseEntity(HttpStatus.BAD_REQUEST, message, request);
	}

	private static String generateHttpMessageNotReadableExceptionMessage(HttpMessageNotReadableException ex) {
		String message = ex.getMessage();
		Throwable cause = ex.getCause();
		if (cause instanceof MismatchedInputException e) {
			return generateMismatchedInputExceptionMessage(e, message);
		} else if (cause instanceof JsonParseException) {
			return String.format("%s. %s", ErrorMessage.REQUEST_BODY_INVALID, message);
		}
		return message;
	}

	private static String generateMismatchedInputExceptionMessage(MismatchedInputException cause, String message) {
		if (message.startsWith(ErrorMessage.JSON_PARSE_ERROR)) {
			List<Reference> path = cause.getPath();
			if (CollectionUtils.isNotEmpty(path)) {
				Reference reference = path.get(0);
				String className = reference.getFrom().getClass().getSimpleName();
				String fieldName = reference.getFieldName();
				String originalMessage = cause.getOriginalMessage();
				return String.format("%s in %s for field %s: %s",
						ErrorMessage.JSON_PARSE_ERROR, className, fieldName, originalMessage);
			}
		}
		return message;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResponseError> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex, WebRequest request) {
		String message = generateMethodArgumentNotValidExceptionMessage(ex);
		log.info("Invalid client request (method argument not valid): {}", message);
		return responseEntity(HttpStatus.BAD_REQUEST, message, request);
	}

	private static String generateMethodArgumentNotValidExceptionMessage(MethodArgumentNotValidException ex) {
		BindingResult result = ex.getBindingResult();
		if (result != null) {
			List<FieldError> errors = result.getFieldErrors();
			if (CollectionUtils.isNotEmpty(errors)) {
				FieldError e = errors.get(0);
				return String.format("%s. Value '%s' for %s not accepted.",
						e.getDefaultMessage(), e.getRejectedValue(), e.getField());
			}
		}
		return ex.getMessage();
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ResponseError> handleMethodArgumentTypeMismatch(
			MethodArgumentTypeMismatchException ex, WebRequest request) {
		String message = generateMethodArgumentTypeMismatchExceptionMessage(ex);
		log.info("Invalid client request (method argument type mismatch): {}", message);
		return responseEntity(HttpStatus.BAD_REQUEST, message, request);
	}

	private static String generateMethodArgumentTypeMismatchExceptionMessage(
			MethodArgumentTypeMismatchException ex) {
		if (ex.getCause() instanceof IllegalArgumentException e) {
			return String.format("Type mismatch for path variable: %s", e.getMessage());
		}
		return ex.getMessage();
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ResponseError> handleHttpMediaTypeNotSupported(
			HttpMediaTypeNotSupportedException ex, WebRequest request) {
		String message = ex.getMessage();
		log.info("Invalid client request (http media type not supported): {}", message);
		return responseEntity(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message, request);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ResponseError> handleMissingServletRequestParameter(
			MissingServletRequestParameterException ex, WebRequest request) {
		return handleEmptyPathVariableProblem(ex, request);
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ResponseError> handleNoHandlerFound(NoHandlerFoundException ex, WebRequest request) {
		return handleEmptyPathVariableProblem(ex, request);
	}

	@ExceptionHandler(MissingPathVariableException.class)
	public ResponseEntity<ResponseError> handleMissingPathVariable(
			MissingPathVariableException ex, WebRequest request) {
		return handleEmptyPathVariableProblem(ex, request);
	}

	private ResponseEntity<ResponseError> handleEmptyPathVariableProblem(Exception ex, WebRequest request) {
		String message = generateEmptyPathVariableProblemMessage(ex, request);
		log.info("Invalid client request (missing servlet request parameter): {}", message);
		return responseEntity(HttpStatus.BAD_REQUEST, message, request);
	}

	private static String generateEmptyPathVariableProblemMessage(Exception ex, WebRequest request) {
		if (ex instanceof NoHandlerFoundException || ex instanceof MissingPathVariableException) {
			String method = getMethodIfRequestHasEmptyPathVariable(request);
			if (method != null) {
				if (HttpMethod.GET.matches(method)) {
					return ErrorMessage.ID_OR_PARAM_IS_EMPTY;
				} else if (HttpMethod.PUT.matches(method) || HttpMethod.DELETE.matches(method)) {
					return ErrorMessage.ID_IS_EMPTY;
				}
			}
		}
		return ex.getMessage();
	}

	private static String getMethodIfRequestHasEmptyPathVariable(WebRequest request) {
		HttpServletRequest httpServletRequest = ((ServletWebRequest) request).getRequest();
		String uri = httpServletRequest.getRequestURI();
		uri = stripTrailingSpaces(uri);
		if (matchesPlaceUri(uri) && !hasParameter(request)) {
			return httpServletRequest.getMethod();
		}
		return null;
	}

	private static final Pattern TRAILING_SPACES_PATTERN = Pattern.compile("/(\\s|%20)+$");

	private static String stripTrailingSpaces(String uri) {
		return TRAILING_SPACES_PATTERN.matcher(uri).replaceFirst("/");
	}

	private static final Pattern PLACE_URI_PATTERN = Pattern.compile("/(place_offers|place_search)/?");

	private static boolean matchesPlaceUri(String uri) {
		return PLACE_URI_PATTERN.matcher(uri).matches();
	}

	private static boolean hasParameter(WebRequest request) {
		Map<String, String[]> parameterMap = request.getParameterMap();
		return parameterMap != null
				&& parameterMap
					.keySet()
					.stream()
					.anyMatch(StringUtils::isNotBlank);
	}

	@ExceptionHandler(OfferValidationProblem.class)
	public ResponseEntity<ResponseError> handleOfferValidationProblem(
			OfferValidationProblem ex, WebRequest request) {
		String message = ex.getMessage();
		log.info("Invalid client request (PlaceOffer validation problem): {}", ex.getMessage());
		return responseEntity(HttpStatus.BAD_REQUEST, message, request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ResponseError> handleDataIntegrityViolation(
			DataIntegrityViolationException ex, WebRequest request) {
		String message = generateDataIntegrityViolationExceptionMessage(ex, request);
		log.info("Invalid client request (data integrity violation): {}", message);
		return responseEntity(HttpStatus.BAD_REQUEST, message, request);
	}

	private static String generateDataIntegrityViolationExceptionMessage(Exception ex, WebRequest request) {
		String message = ex.getMessage();
		if (ex.getCause() instanceof ConstraintViolationException e
				&& ConstraintKind.UNIQUE.equals(e.getKind())
				&& (message.contains("insert into accommodation") || message.contains("update accommodation"))) {
			return "Accommodation title must be unique for the user";
		}
		return message;
	}

	@ExceptionHandler(WebClientRequestException.class)
	public ResponseEntity<ResponseError> handleWebClientRequestException(
			WebClientRequestException ex, WebRequest request) {
		return handleWebClientProblem(ex, request);
	}

	@ExceptionHandler(WebClientResponseException.class)
	public ResponseEntity<ResponseError> handleWebClientResponseException(
			WebClientResponseException ex, WebRequest request) {
		return handleWebClientProblem(ex, request);
	}
	
	private ResponseEntity<ResponseError> handleWebClientProblem(Exception ex, WebRequest request) {
		String message = ex.getMessage();
		log.error("WebClient problem: {}", message, ex);
		message = String.format("%s: %s", ex.getClass().getName(), message);
		return responseEntity(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
	}

	private static ResponseEntity<ResponseError> responseEntity(
			HttpStatus status, String message, WebRequest request) {
		return ResponseEntity
					.status(status)
					.body(new ResponseError(status, message, request));
	}
}
