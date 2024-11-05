package com.github.kat_ka.spend_the_night.endpoints

import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.HOST1
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.HOST2
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.PLACE_OFFERS_URI

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.http.MediaType.TEXT_XML

import spock.lang.*

@Title("Test the REST API endpoint /place_offers/{id} using the PUT method")
@Stepwise
class PlaceOfferPutRequestTest extends RequestSpecification {

	private existingId

	private existingId() {
		existingId = (existingId != null) ? existingId : getId(fromJson(doPost(PLACE_OFFERS_URI, basicPlaceOffer())))
	}

	private uri

	private uri() {
		uri = (uri != null) ? uri : "$PLACE_OFFERS_URI/${existingId()}"
	}

	def "A valid put request should work and return 200"() {
		when:
			def updatePlaceOffer = (example == 1) ? fromJson(basicPlaceOffer()) : fromJson(detailedPlaceOffer())
			updatePlaceOffer.accommodation.description = "category: A-$example"

			def response = doPut(uri(), toJson(updatePlaceOffer))

		then:
			resultIs(response, OK)

		when:
			response = doGet(uri())
			final getResponsePlaceOffer = fromJson(response)
			updatePlaceOffer.accommodation.id = existingId()

		then:
			getResponsePlaceOffer == updatePlaceOffer

		where:
			example | _
			1       | _
			2       | _
	}

	def "A put request with a non-existing id should respond with status code 404"() {
		given:
			final id = UUID.randomUUID()

		when:
			final putResponse = doPut("$PLACE_OFFERS_URI/$id", basicPlaceOffer())
			final getResponse = doGet("$PLACE_OFFERS_URI/$id")

		then:
			resultIs(putResponse, NOT_FOUND)
			resultIs(getResponse, NOT_FOUND)
	}

	def "A put request with an invalid UUID or an empty id should respond with status code 400 and a meaningful error message"() {
		when:
			final response = doPut("$PLACE_OFFERS_URI/$id", basicPlaceOffer())

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs("$PLACE_OFFERS_URI/$id", responseError, BAD_REQUEST, expectedMessage)

		where:
			id        | expectedMessage
			'corrupt' | "Type mismatch for path variable: Invalid UUID string: $id"
			''        | 'Required path variable id is missing'
			'  '      | 'Required path variable id is missing'
	}

	def "A put request with an empty body should respond with status code 400 and a meaningful error message"() {
		given:
			final emptyRequestBody = ''

		when:
			final response = doPut(uri(), emptyRequestBody)

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(uri(), responseError, BAD_REQUEST)
			responseError.message.startsWith('Required request body is missing')
	}

	def "A put request with invalid JSON should respond with status code 400 and a meaningful error message"() {
		given:
			final invalidJson = '{]}'

		when:
			final response = doPut(uri(), invalidJson)

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(uri(), responseError, BAD_REQUEST)
			responseError.message.startsWith('Invalid request body. JSON parse error')
	}

	def "A put request with a not accepted content type should respond with status code 415 and a meaningful error message"() {
		given:
			final placeOfferAsXml = '<placeOffer><userName>Joe</userName></placeOffer>'

		when:
			final response = doPut(uri(), placeOfferAsXml, TEXT_XML)

		then:
			resultIs(response, UNSUPPORTED_MEDIA_TYPE)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(uri(), responseError, UNSUPPORTED_MEDIA_TYPE, "Content-Type 'text/xml' is not supported")
	}

	def "A put request with a wrong field type in the body should respond with status code 400 and a meaningful error message"() {
		given:
			final invalidPlaceOffer = [userName: []]

		when:
			final response = doPut(uri(), toJson(invalidPlaceOffer))

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(uri(), responseError, BAD_REQUEST)
			responseError.message.startsWith('JSON parse error in PlaceOffer for field userName: Cannot deserialize value')
	}

	def "A put request with umlaut should work and return 200"() {
		given:
			def updatePlaceOffer = fromJson(detailedPlaceOffer())
			updatePlaceOffer.accommodation.description = 'category: \u00fc' // ue

		when:
			def response = doPut(uri(), toJson(updatePlaceOffer))

		then:
			resultIs(response, OK)

		when:
			response = doGet(uri())
			final getResponsePlaceOffer = fromJson(response)
			updatePlaceOffer.accommodation.id = existingId()

		then:
			getResponsePlaceOffer == updatePlaceOffer
	}

	def "A put request with the same accommodation title used for another place offer for this user should respond with status code 400 and a meaningful error message"() {
		given:
			def placeOffer1 = fromJson(basicPlaceOffer())
			def placeOffer2 = fromJson(basicPlaceOffer())

		when:
			doPost(PLACE_OFFERS_URI, toJson(placeOffer1))
			String id2 = getId(fromJson(doPost(PLACE_OFFERS_URI, toJson(placeOffer2))))

		and:
			placeOffer2.accommodation.title = placeOffer1.accommodation.title
			final response = doPut("$PLACE_OFFERS_URI/$id2", toJson(placeOffer2))

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs("$PLACE_OFFERS_URI/$id2", responseError, BAD_REQUEST, 'Accommodation title must be unique for the user')
	}

	def "A put request with the same accommodation title for different users should work and return 201"() {
		given:
			def placeOffer1 = fromJson(basicPlaceOffer())
			placeOffer1.userName = HOST1

			def placeOffer2 = fromJson(basicPlaceOffer())
			placeOffer2.userName = HOST2

		when:
			String id1 = getId(fromJson(doPost(PLACE_OFFERS_URI, toJson(placeOffer1), APPLICATION_JSON, getToken(HOST1))))
			String id2 = getId(fromJson(doPost(PLACE_OFFERS_URI, toJson(placeOffer2), APPLICATION_JSON, getToken(HOST2))))

		and:
			placeOffer2.userName = HOST1
			final response = doPut("$PLACE_OFFERS_URI/$id1", toJson(placeOffer2), APPLICATION_JSON, getToken(HOST1))

		then:
			resultIs(response, OK)

		when:
			final response1 = doGet("$PLACE_OFFERS_URI/$id1", getToken(HOST1))
			final response2 = doGet("$PLACE_OFFERS_URI/$id2", getToken(HOST2))

		then:
			resultIs(response1, OK)
			resultIs(response2, OK)

		when:
			final responsePlaceOffer1 = fromJson(response1)
			final responsePlaceOffer2 = fromJson(response2)

		then:
			responsePlaceOffer1.accommodation.title == responsePlaceOffer2.accommodation.title
	}
}
