package com.github.kat_ka.spend_the_night.endpoints

import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.PLACE_OFFERS_URI
import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK

import spock.lang.*

@Title("Test the REST API endpoint /place_offers/{id} using the GET method")
class PlaceOfferGetRequestTest extends RequestSpecification {

	def "A get request with an existing id should return the corresponding PlaceOffer along with status code 200"() {
		when:
			def placeOffer = (example == 1) ? basicPlaceOffer() : detailedPlaceOffer()
			final id = getId(fromJson(doPost(PLACE_OFFERS_URI, placeOffer)))

			final response = doGet("$PLACE_OFFERS_URI/$id")

		then:
			resultIs(response, OK)

		when:
			final responsePlaceOffer = fromJson(response)
			placeOffer = fromJson(placeOffer)
			placeOffer.accommodation.id = id

		then:
			with (responsePlaceOffer) {
				accommodation.id instanceof UUID
				accommodation.id != null
			}
			responsePlaceOffer == placeOffer

		where:
			example | _
			1       | _
			2       | _
	}

	def "A get request with a non-existing id should respond with status code 404"() {
		given:
			final id = UUID.randomUUID()

		when:
			final response = doGet("$PLACE_OFFERS_URI/$id")

		then:
			resultIs(response, NOT_FOUND)
	}

	def "A get request with an invalid UUID or an empty id should respond with status code 400 and a meaningful error message"() {
		when:
			final response = doGet("$PLACE_OFFERS_URI/$id")

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs("$PLACE_OFFERS_URI/$id", responseError, BAD_REQUEST, expectedMessage)

		where:
			id        | expectedMessage
			'invalid' | "Type mismatch for path variable: Invalid UUID string: $id"
			''        | 'Required path variable id or query parameter is missing'
			'  '      | 'Required path variable id or query parameter is missing'
	}
}
