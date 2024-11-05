package com.github.kat_ka.spend_the_night.endpoints

import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.PLACE_OFFERS_URI

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.NOT_FOUND

import spock.lang.*

@Title("Test the REST API endpoint /place_offers/{id} using the DELETE method")
class PlaceOfferDeleteRequestTest extends RequestSpecification {

	def "A delete request with an existing id should work and respond with status code 204"() {
		when:
			final placeOffer = (example == 1) ? basicPlaceOffer() : detailedPlaceOffer()
			final id = getId(fromJson(doPost(PLACE_OFFERS_URI, placeOffer)))

			final deleteResponse = doDelete("$PLACE_OFFERS_URI/$id")
			final getResponse = doGet("$PLACE_OFFERS_URI/$id")

		then:
			resultIs(deleteResponse, NO_CONTENT)
			resultIs(getResponse,    NOT_FOUND)

		where:
			example | _
			1       | _
			2       | _
	}

	def "A delete request with a non-existing id should respond with status code 404"() {
		given:
			final id = UUID.randomUUID()

		when:
			final deleteResponse = doDelete("$PLACE_OFFERS_URI/$id")
			final getResponse = doGet("$PLACE_OFFERS_URI/$id")

		then:
			resultIs(deleteResponse, NOT_FOUND)
			resultIs(getResponse,    NOT_FOUND)
	}

	def "A delete request with an invalid UUID or an empty id should respond with status code 400 and a meaningful error message"() {
		when:
			final deleteResponse = doDelete("$PLACE_OFFERS_URI/$id")

		then:
			resultIs(deleteResponse, BAD_REQUEST)

		when:
			final responseError = fromJson(deleteResponse, Map)

		then:
			resultContentIs("$PLACE_OFFERS_URI/$id", responseError, BAD_REQUEST, expectedMessage)

		when:
			final getResponse = doGet("$PLACE_OFFERS_URI/$id")

		then:
			resultIs(getResponse, BAD_REQUEST)

		where:
			id        | expectedMessage
			'invalid' | "Type mismatch for path variable: Invalid UUID string: $id"
			''        | 'Required path variable id is missing'
			'  '      | 'Required path variable id is missing'
	}
}
