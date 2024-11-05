package com.github.kat_ka.spend_the_night.exception;

public final class ErrorMessage {

	private ErrorMessage() {}

	protected static final String REQUEST_BODY_INVALID = "Invalid request body";

	protected static final String JSON_PARSE_ERROR = "JSON parse error";

	protected static final String ID_IS_EMPTY = "Required path variable id is missing";

	protected static final String ID_OR_PARAM_IS_EMPTY = "Required path variable id or query parameter is missing";
}
