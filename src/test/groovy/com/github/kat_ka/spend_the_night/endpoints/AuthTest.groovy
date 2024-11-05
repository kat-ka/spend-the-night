package com.github.kat_ka.spend_the_night.endpoints

import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.GUEST1
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.HOST1
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.HOST2
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.PLACE_OFFERS_URI
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.PLACE_SEARCH_URI
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.TOWN
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.USER_DELETED
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.USER_NAME
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.USER_NOT_ALLOWED
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.USER_RENAMED

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNAUTHORIZED
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE

import org.apache.hc.core5.http.io.entity.EntityUtils

import org.springframework.beans.factory.annotation.Value

import spock.lang.*

@Title("Test the REST API user authorization")
@Stepwise
class AuthTest extends RequestSpecification {

	@Shared
	private token

	@Value('${custom.auth.server.client.token.expired}')
	private tokenExpired

	@Value('${custom.auth.server.client.token.user-renamed}')
	private tokenRenamedUser

	@Value('${custom.auth.server.client.token.user-deleted}')
	private tokenDeletedUser

	private static final TOKEN_PATTERN = /(?i)([a-z0-9]+\.){2}[a-z0-9\_\-]+/

	def "The authorization server should return an access token"() {
		when:
			final response = authServerResponse()
			final responseEntity = response.entity
			final responseString = EntityUtils.toString(responseEntity)
			final responseMap = fromJson(responseString, Map)

			final status = response.code
			final contentType = responseEntity.contentType
			token = responseMap.access_token

		then:
			status == OK.value
			contentType == APPLICATION_JSON_VALUE
			token != null
			token.matches(TOKEN_PATTERN)
	}

	def "A request without authorization header should respond with status code 401 and a meaningful error message"() {
		when:
			final response = doPostWithoutAuthHeader(PLACE_OFFERS_URI)

		then:
			resultIs(response, UNAUTHORIZED)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, UNAUTHORIZED, 'Insufficient authentication: Full authentication is required to access this resource')
	}

	def "A request with an invalid access token should respond with status code 401 and a meaningful error message"() {
		when:
			final response = doPost(PLACE_OFFERS_URI, basicPlaceOffer(), APPLICATION_JSON, invalidToken)

		then:
			resultIs(response, UNAUTHORIZED)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, UNAUTHORIZED, "An error occurred while attempting to decode the Jwt: $errorDetail")

		where:
			invalidToken                            | errorDetail
			'a.b.c'                                 | 'Malformed token'
			'.'                                     | 'Malformed token'
			'a1.b.c.d'                              | 'Malformed token'
			'a2a.b'                                 | 'Malformed token'
			'eyJ.eyJ.123'                           | 'Malformed token'
			'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.' | 'Malformed token'
			'0'                                     | 'Malformed token'

			"X$token"                               | 'Malformed token'
			"$token.$token"                         | 'Malformed token'
			"e$token"                               | 'Malformed token'
			"$token."                               | 'Malformed token'
			"${token}3"                             | 'Signed JWT rejected: Invalid signature'
			"$token$token"                          | 'Malformed token'

			"${token.replaceFirst(/\./, '')}"       | 'Malformed token'
			"${token.replaceFirst(/\./, '..')}"     | 'Malformed token'
			"${token.replaceFirst(/\./, '.X')}"     | 'Malformed payload'
	}

	def "A request with a malformed or empty access token should respond with status code 401 and a meaningful error message in the response header"() {
		when:
			final response = doPost(PLACE_OFFERS_URI, basicPlaceOffer(), APPLICATION_JSON, malformedToken)

		then:
			resultIs(response, UNAUTHORIZED)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, UNAUTHORIZED, 'Bearer token is malformed')

		where:
			malformedToken  | _
			'a.b.c*'        | _
			'a.b.c!'        | _
			'?.'            | _
			'&'             | _
			"$token "       | _
			"$token $token" | _
			' '             | _
	}

	def "A request with a malformed authorization header value should respond with status code 401 and a meaningful error message in the response header"() {
		when:
			final response = doPostWithAuthHeaderValue(PLACE_OFFERS_URI, malformedAuthHeaderValue)

		then:
			resultIs(response, UNAUTHORIZED)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, UNAUTHORIZED, 'Bearer token is malformed')

		where:
			malformedAuthHeaderValue | _
			"Bearer  $token"         | _
			"Bearer$token"           | _
			"Bearerr $token"         | _
			"Bearer Bearer $token"   | _
	}

	def "A request with an invalid or empty authorization header value should respond with status code 401 and a meaningful error message"() {
		when:
			final response = doPostWithAuthHeaderValue(PLACE_OFFERS_URI, invalidAuthHeaderValue)

		then:
			resultIs(response, UNAUTHORIZED)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, UNAUTHORIZED, 'Insufficient authentication: Full authentication is required to access this resource')

		where:
			invalidAuthHeaderValue | _
			token                  | _
			" Bearer $token"       | _
			" $token"              | _
			"$token "              | _
			''                     | _
			' '                    | _
	}

	def "A request with an invalid or empty authorization header name should respond with status code 401 and a meaningful error message"() {
		when:
			final response = doPostWithAuthHeaderName(PLACE_OFFERS_URI, invalidAuthHeaderName)

		then:
			resultIs(response, UNAUTHORIZED)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, UNAUTHORIZED, 'Insufficient authentication: Full authentication is required to access this resource')

		where:
			invalidAuthHeaderName | _
			'Authorization '      | _
			' Authorization'      | _
			'Authorizatio'        | _
			'&=;'                 | _
			' '                   | _
	}

	def "A request with an access token for a valid user but with additional spaces should work"() {
		when:
			final userNameWithSpaces = "  $HOST1 "

			def placeOffer = fromJson(basicPlaceOffer())
			placeOffer.userName = userNameWithSpaces
			placeOffer = toJson(placeOffer)

			final id = getId(fromJson(doPost(PLACE_OFFERS_URI, basicPlaceOffer())))

			final path = getPath(requestCase, id)
			final response = switch(requestCase) {
				case 'post'              -> doPost(path, placeOffer)
				case 'put'               -> doPut(path, placeOffer)
				case 'get user offers'   -> doGetWith3Params(path, USER_NAME, userNameWithSpaces)
				case 'get search result' -> doGetWith4Params(path, USER_NAME, userNameWithSpaces, TOWN, 'Kiel')
			}

		then:
			resultIs(response, status)

		where:
			requestCase         | status
			'post'              | CREATED
			'put'               | OK
			'get user offers'   | OK

			'get search result' | OK
	}

	def "A request with an access token for the wrong user should respond with status code 403 and a meaningful error message"() {
		when:
			def placeOffer = fromJson(basicPlaceOffer())
			placeOffer.userName = HOST1
			placeOffer = toJson(placeOffer)

			final id = getId(fromJson(doPost(PLACE_OFFERS_URI, placeOffer, APPLICATION_JSON, getToken(HOST1))))

			final path = getPath(requestCase, id)
			final response = switch(requestCase) {
				case 'post'              -> doPost(path, placeOffer, APPLICATION_JSON, getToken(HOST2))
				case 'put'               -> doPut(path, placeOffer, APPLICATION_JSON, getToken(HOST2))
				case 'delete'            -> doDelete(path, getToken(HOST2))
				case 'get offer by id'   -> doGet(path, getToken(HOST2))
				case 'get user offers'   -> doGetWith3Params(path, USER_NAME, HOST1, ' ', '', ' ', '', getToken(HOST2))
				case 'get search result' -> doGetWith4Params(path, USER_NAME, HOST1, TOWN, 'Kiel', ' ', '', ' ', '', getToken(HOST2))
			}

		then:
			resultIs(response, FORBIDDEN)

		when:	
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(path, responseError, FORBIDDEN, "The token is valid but the user doesn't match.")

		where:
			requestCase        | _
			'post'             | _
			'put'              | _
			'delete'           | _
			'get offer by id'  | _
			'get user offers'  | _

			'get search result'| _
	}

	def "A request with an access token for a non-existing user should respond with status code 403 and a meaningful error message"() {
		when:
			final nonexistingUserName = 'i_dont_exist'

			def placeOffer = fromJson(basicPlaceOffer())
			placeOffer.userName = nonexistingUserName
			placeOffer = toJson(placeOffer)

			final id = getId(fromJson(doPost(PLACE_OFFERS_URI, basicPlaceOffer())))

			final path = getPath(requestCase, id)
			final response = switch(requestCase) {
				case 'post'              -> doPost(path, placeOffer)
				case 'put'               -> doPut(path, placeOffer)
				case 'get user offers'   -> doGetWith3Params(path, USER_NAME, nonexistingUserName)
				case 'get search result' -> doGetWith4Params(path, USER_NAME, nonexistingUserName, TOWN, 'Kiel')
			}

		then:
			resultIs(response, FORBIDDEN)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(path, responseError, FORBIDDEN, "The token is valid but the user doesn't exist.")

		where:
			requestCase         | _
			'post'              | _
			'put'               | _
			'get user offers'   | _

			'get search result' | _
	}

	def "A request with an access token for an empty user name should respond with status code 400 and a meaningful error message"() {
		when:
			final emptyUserName = ' '

			def placeOffer = fromJson(basicPlaceOffer())
			placeOffer.userName = emptyUserName
			placeOffer = toJson(placeOffer)

			final id = getId(fromJson(doPost(PLACE_OFFERS_URI, basicPlaceOffer())))

			final path = getPath(requestCase, id)
			final response = switch(requestCase) {
				case 'post'              -> doPost(path, placeOffer)
				case 'put'               -> doPut(path, placeOffer)
				case 'get user offers'   -> doGetWith3Params(path, USER_NAME, emptyUserName)
				case 'get search result' -> doGetWith4Params(path, USER_NAME, emptyUserName, TOWN, 'Kiel')
			}

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(path, responseError, BAD_REQUEST, expectedMessage)

		where:
			requestCase         | expectedMessage
			'post'              | "The user name must not be empty. Value ' ' for userName not accepted."
			'put'               | "The user name must not be empty. Value ' ' for userName not accepted."
			'get user offers'   | "Required request parameter 'userName' is not present"

			'get search result' | "Required request parameter 'userName' is not present"
	}

	def "A request with an access token for an user name with value null should respond with status code 403 and a meaningful error message"() {
		when:
			final nullUserName = null

			def placeOffer = fromJson(detailedPlaceOffer())
			placeOffer.userName = nullUserName
			placeOffer = toJson(placeOffer)

			final id = getId(fromJson(doPost(PLACE_OFFERS_URI, basicPlaceOffer())))

			final path = getPath(requestCase, id)
			final response = switch(requestCase) {
				case 'post'              -> doPost(path, placeOffer)
				case 'put'               -> doPut(path, placeOffer)
				case 'get user offers'   -> doGetWith3Params(path, 'nothing','')
				case 'get search result' -> doGetWith4Params(path, ' ', '', TOWN, 'Kiel')
			}

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(path, responseError, BAD_REQUEST, expectedMessage)

		where:
			requestCase         | expectedMessage
			'post'              | "The user name must not be empty. Value 'null' for userName not accepted."
			'put'               | "The user name must not be empty. Value 'null' for userName not accepted."
			'get user offers'   | "Required request parameter 'userName' for method parameter type String is not present"

			'get search result' | "Required request parameter 'userName' for method parameter type String is not present"
	}

	def "A request with an access token not in the scope should respond with status code 403 and a meaningful error message"() {
		when:
			final user = USER_NOT_ALLOWED

			final id = getId(fromJson(doPost(PLACE_OFFERS_URI, basicPlaceOffer())))

			final path = getPath(requestCase, id)
			final response = switch(requestCase) {
				case 'post'              -> doPost(path, '{}', APPLICATION_JSON, getToken(user))
				case 'put'               -> doPut(path, '{}', APPLICATION_JSON, getToken(user))
				case 'delete'            -> doDelete(path, getToken(user))
				case 'get offer by id'   -> doGet(path, getToken(user))
				case 'get user offers'   -> doGetWith3Params(path, USER_NAME, user, ' ', '', ' ', '', getToken(user))
				case 'get search result' -> doGetWith4Params(path, USER_NAME, user, TOWN, 'Kiel', ' ', '', ' ', '', getToken(user))
			}

		then:
			resultIs(response, FORBIDDEN)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(path, responseError, FORBIDDEN, 'Access Denied')

		where:
			requestCase        | _
			'post'             | _
			'put'              | _
			'delete'           | _
			'get offer by id'  | _
			'get user offers'  | _

			'get search result'| _
	}

	def "A request with an access token in the wrong scope should respond with status code 403 and a meaningful error message"() {
		when:
			final user = GUEST1

			final id = getId(fromJson(doPost(PLACE_OFFERS_URI, basicPlaceOffer())))

			final path = getPath(requestCase, id)
			final response = switch(requestCase) {
				case 'post'              -> doPost(path, '{}', APPLICATION_JSON, getToken(user))
				case 'put'               -> doPut(path, '{}', APPLICATION_JSON, getToken(user))
				case 'delete'            -> doDelete(path, getToken(user))
				case 'get offer by id'   -> doGet(path, getToken(user))
				case 'get user offers'   -> doGetWith3Params(path, USER_NAME, user, ' ', '', ' ', '', getToken(user))
			}

		then:
			resultIs(response, FORBIDDEN)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(path, responseError, FORBIDDEN, 'Access Denied')

		where:
			requestCase        | _
			'post'             | _
			'put'              | _
			'delete'           | _
			'get offer by id'  | _
			'get user offers'  | _
	}

	def "A request with an expired access token should respond with status code 401 and a meaningful error message"() {
		when:
			final response = doPost(PLACE_OFFERS_URI, basicPlaceOffer(), APPLICATION_JSON, tokenExpired)

		then:
			resultIs(response, UNAUTHORIZED)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, UNAUTHORIZED)
			responseError.message.startsWith('An error occurred while attempting to decode the Jwt: Jwt expired')
	}

	def "A request with an access token for a deleted client should respond with status code 401 and a meaningful error message"() {
		when:
			final user = USER_DELETED

			def placeOffer = fromJson(detailedPlaceOffer())
			placeOffer.userName = user
			placeOffer = toJson(placeOffer)

			final id = getId(fromJson(doPost(PLACE_OFFERS_URI, basicPlaceOffer())))

			final path = getPath(requestCase, id)
			final response = switch(requestCase) {
				case 'post'              -> doPost(path, placeOffer, APPLICATION_JSON, tokenDeletedUser)
				case 'put'               -> doPut(path, placeOffer, APPLICATION_JSON, tokenDeletedUser)
				case 'delete'            -> doDelete(path, tokenDeletedUser)
				case 'get offer by id'   -> doGet(path, tokenDeletedUser)
				case 'get user offers'   -> doGetWith3Params(path, USER_NAME, user, ' ', '', ' ', '', tokenDeletedUser)
				case 'get search result' -> doGetWith4Params(path, USER_NAME, user, TOWN, 'Kiel', ' ', '', ' ', '', tokenDeletedUser)
			}

		then:
			resultIs(response, UNAUTHORIZED)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(path, responseError, UNAUTHORIZED)
			responseError.message.startsWith('An error occurred while attempting to decode the Jwt: Signed JWT rejected')

		where:
			requestCase        | _
			'post'             | _
			'put'              | _
			'delete'           | _
			'get offer by id'  | _
			'get user offers'  | _

			'get search result'| _
	}

	def "A request with an access token for a client with a changed client id should respond with status code 401 and a meaningful error message"() {
		when:
			final user = USER_RENAMED

			def placeOffer = fromJson(detailedPlaceOffer())
			placeOffer.userName = user
			placeOffer = toJson(placeOffer)

			final id = getId(fromJson(doPost(PLACE_OFFERS_URI, basicPlaceOffer())))

			final path = getPath(requestCase, id)
			final response = switch(requestCase) {
				case 'post'              -> doPost(path, placeOffer, APPLICATION_JSON, tokenRenamedUser)
				case 'put'               -> doPut(path, placeOffer, APPLICATION_JSON, tokenRenamedUser)
				case 'delete'            -> doDelete(path, tokenRenamedUser)
				case 'get offer by id'   -> doGet(path, tokenRenamedUser)
				case 'get user offers'   -> doGetWith3Params(path, USER_NAME, user, ' ', '', ' ', '', tokenRenamedUser)
				case 'get search result' -> doGetWith4Params(path, USER_NAME, user, TOWN, 'Kiel', ' ', '', ' ', '', tokenRenamedUser)
			}

		then:
			resultIs(response, UNAUTHORIZED)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(path, responseError, UNAUTHORIZED)
			responseError.message.startsWith('An error occurred while attempting to decode the Jwt: Signed JWT rejected')

		where:
			requestCase        | _
			'post'             | _
			'put'              | _
			'delete'           | _
			'get offer by id'  | _
			'get user offers'  | _

			'get search result'| _
	}

	private getPath(requestCase, id) {
		switch(requestCase) {
			case ['post', 'get user offers']          -> PLACE_OFFERS_URI
			case ['put', 'delete', 'get offer by id'] -> "$PLACE_OFFERS_URI/$id"
			case 'get search result'                  -> PLACE_SEARCH_URI
		}
	}
}
