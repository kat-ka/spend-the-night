package com.github.kat_ka.spend_the_night.endpoints

import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.HOST1
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.HOST2
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.PLACE_OFFERS_URI
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.SIZE
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.USER_NAME

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.http.MediaType.TEXT_XML

import com.github.kat_ka.spend_the_night.model.item.Accommodation
import com.github.kat_ka.spend_the_night.model.item.Currency
import com.github.kat_ka.spend_the_night.model.item.PetsAllowed
import com.github.kat_ka.spend_the_night.model.item.SmokingAllowed

import java.time.LocalDate
import java.time.ZoneId

import spock.lang.*

@Title("Test the REST API endpoint /place_offers using the POST method")
class PlaceOfferPostRequestTest extends RequestSpecification {

	def "A valid post request should work and return 201"() {
		when:
			def requestPlaceOffer = (example == 1) ? basicPlaceOffer() : detailedPlaceOffer()
			def response = doPost(PLACE_OFFERS_URI, requestPlaceOffer)

		then:
			resultIs(response, CREATED)

		when:
			final postResponsePlaceOffer = fromJson(response)
			requestPlaceOffer = fromJson(requestPlaceOffer)

		and:
			final id = getId(postResponsePlaceOffer)
			requestPlaceOffer.accommodation.id = id

		then:
			id instanceof UUID
			id != null
			postResponsePlaceOffer == requestPlaceOffer

		when:
			response = doGet("$PLACE_OFFERS_URI/$id")
			final getResponsePlaceOffer = fromJson(response)

		then:
			getResponsePlaceOffer.accommodation.id == id
			getResponsePlaceOffer == requestPlaceOffer

		where:
			example | _
			1       | _
			2       | _
	}

	def "PlaceOffer requests should only manage PlaceOffers from this user"() {
		given:
			def placeOffer1 = basicPlaceOffer(HOST1)
			def placeOffer2 = detailedPlaceOffer(HOST2)

		when:
			String id1 = getId(fromJson(doPost(PLACE_OFFERS_URI, placeOffer1, APPLICATION_JSON, getToken(HOST1))))
			String id2 = getId(fromJson(doPost(PLACE_OFFERS_URI, placeOffer2, APPLICATION_JSON, getToken(HOST2))))

		and:
			def response1 = doGet("$PLACE_OFFERS_URI/$id1", getToken(HOST2))
			def response2 = doGet("$PLACE_OFFERS_URI/$id2", getToken(HOST1))
			def response3 = doGet("$PLACE_OFFERS_URI/$id1", getToken(HOST1))
			def response4 = doGet("$PLACE_OFFERS_URI/$id2", getToken(HOST2))

		then:
			resultIs(response1, FORBIDDEN)
			resultIs(response2, FORBIDDEN)
			resultIs(response3, OK)
			resultIs(response4, OK)

		when:
			response1 = doGetWith3Params(PLACE_OFFERS_URI, USER_NAME, HOST1, SIZE, maxPageSize, ' ', '', getToken(HOST1))
			response2 = doGetWith3Params(PLACE_OFFERS_URI, USER_NAME, HOST2, SIZE, maxPageSize, ' ', '', getToken(HOST2))

		then:
			resultIs(response1, OK)
			resultIs(response2, OK)

		when:
			final responsePlaceOffers1 = fromJson(response1, Map)
			final responsePlaceOffers2 = fromJson(response2, Map)

		then:
			responsePlaceOffers1.content.each { assert getId(it) != id2 }
			responsePlaceOffers2.content.each { assert getId(it) != id1 }
			assert responsePlaceOffers1.content.any  { getId(it) == id1 }
			assert responsePlaceOffers2.content.any  { getId(it) == id2 }

		when:
			response1 = doPut("$PLACE_OFFERS_URI/$id1", placeOffer1, APPLICATION_JSON, getToken(HOST2))
			response2 = doPut("$PLACE_OFFERS_URI/$id2", placeOffer2, APPLICATION_JSON, getToken(HOST1))
			response3 = doPut("$PLACE_OFFERS_URI/$id1", placeOffer1, APPLICATION_JSON, getToken(HOST1))
			response4 = doPut("$PLACE_OFFERS_URI/$id2", placeOffer2, APPLICATION_JSON, getToken(HOST2))

		then:
			resultIs(response1, FORBIDDEN)
			resultIs(response2, FORBIDDEN)
			resultIs(response3, OK)
			resultIs(response4, OK)

		when:
			response1 = doDelete("$PLACE_OFFERS_URI/$id1", getToken(HOST2))
			response2 = doDelete("$PLACE_OFFERS_URI/$id2", getToken(HOST1))
			response3 = doDelete("$PLACE_OFFERS_URI/$id1", getToken(HOST1))
			response4 = doDelete("$PLACE_OFFERS_URI/$id2", getToken(HOST2))

		then:
			resultIs(response1, FORBIDDEN)
			resultIs(response2, FORBIDDEN)
			resultIs(response3, NO_CONTENT)
			resultIs(response4, NO_CONTENT)
	}

	def "A post request with an empty request body should respond with status code 400 and a meaningful error message"() {
		given:
			final emptyRequestBody = ''

		when:
			final response = doPost(PLACE_OFFERS_URI, emptyRequestBody)

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, BAD_REQUEST)
			responseError.message.startsWith('Required request body is missing')
	}
	
	def "A post request with a missing request body should respond with status code 400 and a meaningful error message"() {
		when:
			final response = doPostWithoutRequestBody(PLACE_OFFERS_URI)

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, BAD_REQUEST)
			responseError.message.startsWith('Required request body is missing')
	}

	def "A post request with invalid JSON should respond with status code 400 and a meaningful error message"() {
		when:
			final response = doPost(PLACE_OFFERS_URI, invalidJson)

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, BAD_REQUEST)
			responseError.message.startsWith('Invalid request body. JSON parse error: Unexpected character')

		where:
			invalidJson | _
			'{.'        | _
			'{{}'       | _
	}

	def "A post request with a not accepted content type should respond with status code 415 and a meaningful error message"() {
		given:
			final placeOfferAsXml = '<placeOffer><userName>Joe</userName></placeOffer>'

		when:
			final response = doPost(PLACE_OFFERS_URI, placeOfferAsXml, TEXT_XML)

		then:
			resultIs(response, UNSUPPORTED_MEDIA_TYPE)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, UNSUPPORTED_MEDIA_TYPE, "Content-Type 'text/xml' is not supported")
	}

	def "A post request with a wrong field type in the body should respond with status code 400 and a meaningful error message"() {
		given:
			final invalidPlaceOffer = [userName: []]

		when:
			final response = doPost(PLACE_OFFERS_URI, toJson(invalidPlaceOffer))

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, BAD_REQUEST)
			responseError.message.startsWith('JSON parse error in PlaceOffer for field userName: Cannot deserialize value')
	}

	def "A post request with umlaut should work and return 201"() {
		given:
			def requestPlaceOffer = fromJson(detailedPlaceOffer())
			requestPlaceOffer.accommodation.description = 'category: \u00fc' // ue

		when:
			def response = doPost(PLACE_OFFERS_URI, toJson(requestPlaceOffer))

		then:
			resultIs(response, CREATED)

		when:
			final postResponsePlaceOffer = fromJson(response)
			final id = getId(postResponsePlaceOffer)
			requestPlaceOffer.accommodation.id = id

		then:
			id != null
			postResponsePlaceOffer == requestPlaceOffer

		when:
			response = doGet("$PLACE_OFFERS_URI/$id")
			final getResponsePlaceOffer = fromJson(response)

		then:
			getResponsePlaceOffer.accommodation.id == id
			getResponsePlaceOffer == requestPlaceOffer
	}

	def "A post request with an invalid field should respond with status code 400 and a meaningful error message"() {
		when:
			def invalidPlaceOffer = fromJson(detailedPlaceOffer(), Map)
			setFieldValue(invalidPlaceOffer, field, value)

 			final response = doPost(PLACE_OFFERS_URI, toJson(invalidPlaceOffer))

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, BAD_REQUEST)
			responseError.message.startsWith(expectedMessage)

		where:
			field                                           | value             || expectedMessage
			'userName'                                      | [:]               || 'JSON parse error in PlaceOffer for field userName: Cannot deserialize value'
			'accommodation'                                 | 3                 || 'JSON parse error in PlaceOffer for field accommodation: Cannot construct instance'
			'accommodation.title'                           | []                || 'JSON parse error in PlaceOffer for field accommodation: Cannot deserialize value'
			'accommodation.description'                     | []                || 'JSON parse error in PlaceOffer for field accommodation: Cannot deserialize value'
			'accommodation.address'                         | ','               || 'JSON parse error in PlaceOffer for field accommodation: Cannot construct instance'
			'accommodation.address.street'                  | UUID.randomUUID() || 'JSON parse error in PlaceOffer for field accommodation: Cannot construct instance'
			'accommodation.address.street.name'             | []                || 'JSON parse error in PlaceOffer for field accommodation: Cannot deserialize value'
			'accommodation.address.street.number'           | []                || 'JSON parse error in PlaceOffer for field accommodation: Cannot deserialize value'
			'accommodation.address.city'                    | []                || 'JSON parse error in PlaceOffer for field accommodation: Cannot deserialize value'
			'accommodation.address.postalCode'              | []                || 'JSON parse error in PlaceOffer for field accommodation: Cannot deserialize value'
			'accommodation.address.country'                 | []                || 'JSON parse error in PlaceOffer for field accommodation: Cannot deserialize value'
			'accommodation.pictures'                        | 'abcd'            || 'JSON parse error in PlaceOffer for field accommodation: Cannot deserialize value'
			'accommodation.pictures'                        | ['abcd']          || 'JSON parse error in PlaceOffer for field accommodation: Cannot deserialize value'
			'accommodation.pricePerNight'                   | 'one'             || "JSON parse error: Value 'one' for pricePerNight is not an Integer"
			'accommodation.pricePerNight'                   | 3.5               || "JSON parse error: Value '3.5' for pricePerNight is not an Integer"
			'accommodation.pricePerNight'                   | ''                || "JSON parse error: Value '' for pricePerNight is not an Integer"
			'accommodation.pricePerNight'                   | ' '               || "JSON parse error: Value ' ' for pricePerNight is not an Integer"
			'accommodation.pricePerNight'                   | []                || "JSON parse error: Value '[' for pricePerNight is not an Integer"
			'accommodation.currency'                        | 'euro'            || "JSON parse error: Invalid Currency value: euro"
			'accommodation.currency'                        | []                || "JSON parse error: Invalid Currency value: ["
			'accommodation.currency'                        | 4                 || "JSON parse error: Invalid Currency value: 4"
			'accommodation.currency'                        | ''                || "JSON parse error: Invalid Currency value: "
			'accommodation.currency'                        | ' '               || "JSON parse error: Invalid Currency value:  "
			'accommodation.availableDates'                  | 'today'           || 'JSON parse error in PlaceOffer for field accommodation: Cannot deserialize value'
			'accommodation.availableDates'                  | ['today']         || 'JSON parse error in PlaceOffer for field accommodation: Cannot deserialize value'
			'accommodation.availableDates'                  | ['']              || "All accommodation available dates must not be empty. Value 'null' for accommodation.availableDates[] not accepted."
			'accommodation.availableDates'                  | [' ']             || "All accommodation available dates must not be empty. Value 'null' for accommodation.availableDates[] not accepted."
			'accommodation.amenities'                       | 5                 || 'JSON parse error in PlaceOffer for field accommodation: Cannot deserialize value'
			'accommodation.amenities'                       | ''                || 'JSON parse error in PlaceOffer for field accommodation: Cannot coerce empty String'
			'accommodation.amenities'                       | ' '               || 'JSON parse error in PlaceOffer for field accommodation: Cannot coerce empty String'
			'accommodation.amenities'                       | 'wifi'            || "JSON parse error: Invalid Amenity values: wifi"
			'accommodation.amenities'                       | ['wifi/1']        || "JSON parse error: Invalid Amenity value: wifi/1"
			'accommodation.amenities'                       | ['']              || "JSON parse error: Invalid Amenity value: "
			'accommodation.amenities'                       | ['1']             || "JSON parse error: Invalid Amenity value: 1"
			'accommodation.amenities'                       | [3]               || "JSON parse error: Invalid Amenity value: 3"
			'accommodation.hostPreferences'                 | []                || 'JSON parse error in PlaceOffer for field accommodation: Cannot deserialize value'
			'accommodation.hostPreferences'                 | ','               || 'JSON parse error in PlaceOffer for field accommodation: Cannot construct instance'
			'accommodation.hostPreferences.maxGuests'       | 'ten'             || 'JSON parse error in PlaceOffer for field accommodation: Cannot deserialize value'
			'accommodation.hostPreferences.checkInTime'     | '5 pm'            || 'JSON parse error in PlaceOffer for field accommodation: Cannot deserialize value'
			'accommodation.hostPreferences.checkInTime'     | ''                || "The accommodation host preferences check in time must not be empty. Value 'null' for accommodation.hostPreferences.checkInTime not accepted."
			'accommodation.hostPreferences.checkOutTime'    | 10                || 'JSON parse error in PlaceOffer for field accommodation: raw timestamp (10) not allowed for `java.time.LocalTime`'
			'accommodation.hostPreferences.checkOutTime'    | ' '               || "The accommodation host preferences check out time must not be empty. Value 'null' for accommodation.hostPreferences.checkOutTime not accepted."
			'accommodation.hostPreferences.smokingAllowed'  | 'false'           || "JSON parse error: Invalid SmokingAllowed value: false"
			'accommodation.hostPreferences.smokingAllowed'  | []                || "JSON parse error: Invalid SmokingAllowed value: ["
			'accommodation.hostPreferences.smokingAllowed'  | 6                 || "JSON parse error: Invalid SmokingAllowed value: 6"
			'accommodation.hostPreferences.smokingAllowed'  | ''                || "JSON parse error: Invalid SmokingAllowed value: "
			'accommodation.hostPreferences.smokingAllowed'  | ' '               || "JSON parse error: Invalid SmokingAllowed value:  "
			'accommodation.hostPreferences.petsAllowed'     | true              || "JSON parse error: Invalid PetsAllowed value: true"
			'accommodation.hostPreferences.petsAllowed'     | 'MAYBE'           || "JSON parse error: Invalid PetsAllowed value: MAYBE"
			'accommodation.hostPreferences.petsAllowed'     | []                || "JSON parse error: Invalid PetsAllowed value: ["
			'accommodation.hostPreferences.petsAllowed'     | 7                 || "JSON parse error: Invalid PetsAllowed value: 7"
			'accommodation.hostPreferences.petsAllowed'     | ''                || "JSON parse error: Invalid PetsAllowed value: "
			'accommodation.hostPreferences.petsAllowed'     | ' '               || "JSON parse error: Invalid PetsAllowed value:  "
			'accommodation.hostPreferences.languagesSpoken' | 'english'         || "JSON parse error: Invalid Language values: english"
			'accommodation.hostPreferences.languagesSpoken' | 8                 || 'JSON parse error in PlaceOffer for field accommodation: Cannot deserialize value'
			'accommodation.hostPreferences.languagesSpoken' | ''                || 'JSON parse error in PlaceOffer for field accommodation: Cannot coerce empty String'
			'accommodation.hostPreferences.languagesSpoken' | ' '               || 'JSON parse error in PlaceOffer for field accommodation: Cannot coerce empty String'
			'accommodation.hostPreferences.languagesSpoken' | '{}'              || "JSON parse error: Invalid Language values: {}"
			'accommodation.hostPreferences.languagesSpoken' | true              || 'JSON parse error in PlaceOffer for field accommodation: Cannot deserialize value'
			'accommodation.hostPreferences.languagesSpoken' | ['german']        || "JSON parse error: Invalid Language value: german"
			'accommodation.hostPreferences.languagesSpoken' | ['9']             || "JSON parse error: Invalid Language value: 9"
			'accommodation.hostPreferences.languagesSpoken' | [1]               || "JSON parse error: Invalid Language value: 1"
			'accommodation.hostPreferences.languagesSpoken' | ['']              || "JSON parse error: Invalid Language value: "
			'accommodation.hostPreferences.languagesSpoken' | [' ']             || "JSON parse error: Invalid Language value:  "
			'accommodation.hostPreferences.languagesSpoken' | [false]           || "JSON parse error: Invalid Language value: false"
			'accommodation.hostPreferences.languagesSpoken' | ['{}']            || "JSON parse error: Invalid Language value: {}"
	}

	def "A post request with a missing or invalid field should respond with status code 400 and a meaningful error message"() {
		when:
			def invalidPlaceOffer = fromJson(detailedPlaceOffer())
			setFieldValue(invalidPlaceOffer, field, value)

			final response = doPost(PLACE_OFFERS_URI, toJson(invalidPlaceOffer))

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, BAD_REQUEST, expectedMessage)

		where:
			field                                           | value  || expectedMessage
			'userName'                                      | null   || "The user name must not be empty. Value 'null' for userName not accepted."
			'userName'                                      | ''     || "The user name must not be empty. Value '' for userName not accepted."
			'userName'                                      | ' '    || "The user name must not be empty. Value ' ' for userName not accepted."
			'accommodation'                                 | null   || "The accommodation must not be empty. Value 'null' for accommodation not accepted."
			'accommodation.title'                           | null   || "The accommodation title must not be empty. Value 'null' for accommodation.title not accepted."
			'accommodation.title'                           | ''     || "The accommodation title must not be empty. Value '' for accommodation.title not accepted."
			'accommodation.title'                           | ' '    || "The accommodation title must not be empty. Value ' ' for accommodation.title not accepted."
			'accommodation.address'                         | null   || "The accommodation address must not be empty. Value 'null' for accommodation.address not accepted."
			'accommodation.address.street'                  | null   || "The accommodation address street must not be empty. Value 'null' for accommodation.address.street not accepted."
			'accommodation.address.street.name'             | null   || "The accommodation address street name must not be empty. Value 'null' for accommodation.address.street.name not accepted."
			'accommodation.address.street.name'             | ''     || "The accommodation address street name must not be empty. Value '' for accommodation.address.street.name not accepted."
			'accommodation.address.street.name'             | ' '    || "The accommodation address street name must not be empty. Value ' ' for accommodation.address.street.name not accepted."
			'accommodation.address.street.number'           | null   || "The accommodation address street number must not be empty. Value 'null' for accommodation.address.street.number not accepted."
			'accommodation.address.street.number'           | ''     || "The accommodation address street number must not be empty. Value '' for accommodation.address.street.number not accepted."
			'accommodation.address.street.number'           | ' '    || "The accommodation address street number must not be empty. Value ' ' for accommodation.address.street.number not accepted."
			'accommodation.address.city'                    | null   || "The accommodation address city must not be empty. Value 'null' for accommodation.address.city not accepted."
			'accommodation.address.city'                    | ''     || "The accommodation address city must not be empty. Value '' for accommodation.address.city not accepted."
			'accommodation.address.city'                    | ' '    || "The accommodation address city must not be empty. Value ' ' for accommodation.address.city not accepted."
			'accommodation.address.postalCode'              | null   || "The accommodation address postal code must not be empty. Value 'null' for accommodation.address.postalCode not accepted."
			'accommodation.address.postalCode'              | ''     || "The accommodation address postal code must not be empty. Value '' for accommodation.address.postalCode not accepted."
			'accommodation.address.postalCode'              | ' '    || "The accommodation address postal code must not be empty. Value ' ' for accommodation.address.postalCode not accepted."
			'accommodation.address.country'                 | null   || "The accommodation address country must not be empty. Value 'null' for accommodation.address.country not accepted."
			'accommodation.address.country'                 | ''     || "The accommodation address country must not be empty. Value '' for accommodation.address.country not accepted."
			'accommodation.address.country'                 | ' '    || "The accommodation address country must not be empty. Value ' ' for accommodation.address.country not accepted."
			'accommodation.pictures'                        | [null] || "All accommodation pictures must not be empty. Value 'null' for accommodation.pictures[] not accepted."
			'accommodation.pictures'                        | ['']   || "All accommodation pictures must not be empty. Value 'null' for accommodation.pictures[] not accepted."
			'accommodation.pictures'                        | [' ']  || "All accommodation pictures must not be empty. Value 'null' for accommodation.pictures[] not accepted."
			'accommodation.pricePerNight'                   | null   || "The accommodation price per night must not be empty. Value 'null' for accommodation.pricePerNight not accepted."
			'accommodation.pricePerNight'                   | -1     || "The accommodation price per night must not be negative. Value '-1' for accommodation.pricePerNight not accepted."
			'accommodation.availableDates'                  | null   || "Available dates for the accommodation missing. Value 'null' for accommodation.availableDates not accepted."
			'accommodation.availableDates'                  | []     || "Available dates for the accommodation missing. Value '[]' for accommodation.availableDates not accepted."
			'accommodation.availableDates'                  | [null] || "All accommodation available dates must not be empty. Value 'null' for accommodation.availableDates[] not accepted."
			'accommodation.hostPreferences'                 | null   || "The accommodation host preferences must not be empty. Value 'null' for accommodation.hostPreferences not accepted."
			'accommodation.hostPreferences.maxGuests'       | 0      || "The accommodation host preferences max guests number must be at least 1. Value '0' for accommodation.hostPreferences.maxGuests not accepted."
			'accommodation.hostPreferences.checkInTime'     | null   || "The accommodation host preferences check in time must not be empty. Value 'null' for accommodation.hostPreferences.checkInTime not accepted."
			'accommodation.hostPreferences.checkOutTime'    | null   || "The accommodation host preferences check out time must not be empty. Value 'null' for accommodation.hostPreferences.checkOutTime not accepted."
			'accommodation.hostPreferences.languagesSpoken' | null   || "Spoken languages for the accommodation host preferences missing. Value 'null' for accommodation.hostPreferences.languagesSpoken not accepted."
			'accommodation.hostPreferences.languagesSpoken' | []     || "Spoken languages for the accommodation host preferences missing. Value '[]' for accommodation.hostPreferences.languagesSpoken not accepted."
			'accommodation.hostPreferences.languagesSpoken' | [null] || "Spoken languages for the accommodation host preferences missing. Value '[]' for accommodation.hostPreferences.languagesSpoken not accepted."
	}

	private void setFieldValue(placeOffer, fieldPath, value) {
		def fields = fieldPath.split('\\.')
		def currentField = placeOffer
		if (fields.size() > 1) {
			fields[0..-2].each { currentField = currentField?."$it" }
		}
		currentField?."${fields[-1]}" = value
	}

	def "A post request with an empty accommodation should respond with status code 400 and a meaningful error message"() {
		given:
			def invalidPlaceOffer = fromJson(detailedPlaceOffer())
			invalidPlaceOffer.accommodation = new Accommodation()

		when:
			final response = doPost(PLACE_OFFERS_URI, toJson(invalidPlaceOffer))

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, BAD_REQUEST)
			responseError.message ==~ /.*Value 'null' for accommodation.* not accepted./
	}

	def "A valid post request with leading and trailing spaces in values should trim the values correctly and return 201"() {
		given:
			def placeOffer = detailedPlaceOffer()
			def (placeOfferWithoutSpaces, placeOfferWithSpaces) = (0..1).collect { fromJson(placeOffer) }
			addSpacesTo(placeOfferWithSpaces, ['userName',
											   'accommodation.title',
											   'accommodation.description',
											   'accommodation.address.street.name',
											   'accommodation.address.street.number',
											   'accommodation.address.city',
											   'accommodation.address.postalCode',
											   'accommodation.address.country'])

		when:
			def response = doPost(PLACE_OFFERS_URI, toJson(placeOfferWithSpaces))

		then:
			resultIs(response, CREATED)

		when:
			final postResponsePlaceOffer = fromJson(response)
			final id = getId(postResponsePlaceOffer)
			[placeOfferWithoutSpaces, placeOfferWithSpaces].each { it.accommodation.id = id }

		then:
			id != null
			postResponsePlaceOffer == placeOfferWithoutSpaces
			postResponsePlaceOffer != placeOfferWithSpaces

		when:
			response = doGet("$PLACE_OFFERS_URI/$id")
			final getResponsePlaceOffer = fromJson(response)

		then:
			getResponsePlaceOffer.accommodation.id == id
			getResponsePlaceOffer == placeOfferWithoutSpaces
	}

	private void addSpacesTo(placeOffer, List fieldPaths) {
		fieldPaths.each { addSpacesTo(placeOffer, it) }
	}

	private void addSpacesTo(placeOffer, fieldPath) {
		def fields = fieldPath.split('\\.')
		def currentField = placeOffer
		if (fields.size() > 1) {
			fields[0..-2].each { currentField = currentField?."$it" }
		}
		def value = currentField?."${fields[-1]}"
		currentField?."${fields[-1]}" = "  $value  "
	}

	def "A valid post request should evaluate latitude, longitude and timezone correctly and return 201"() {
		given:
			def placeOffer = fromJson(detailedPlaceOffer())
			placeOffer.accommodation.address.with {
				latitude  = null
				longitude = null
				timezone  = null
			}

		when:
			def response = doPost(PLACE_OFFERS_URI, toJson(placeOffer))

		then:
			resultIs(response, CREATED)

		when:
			final postResponsePlaceOffer = fromJson(response)
			final id = getId(postResponsePlaceOffer)

		then:
			id != null
			verifyLatitudeLongitudeAndTimezone(postResponsePlaceOffer)

		when:
			response = doGet("$PLACE_OFFERS_URI/$id")
			final getResponsePlaceOffer = fromJson(response)

		then:
			getResponsePlaceOffer.accommodation.id == id
			verifyLatitudeLongitudeAndTimezone(getResponsePlaceOffer)
	}

	private static final TIMEZONE_EXAMPLE = ZoneId.of('Europe/Berlin')

	private void verifyLatitudeLongitudeAndTimezone(placeOffer) {
		with(placeOffer.accommodation.address) {
			assert latitude  != null
			assert latitude  >= -90
			assert latitude  <= 90
			assert longitude != null
			assert longitude >= -180
			assert longitude <= 180
			assert timezone  != null
			assert timezone  == TIMEZONE_EXAMPLE
		}
	}

	private static final PICTURE_EXAMPLE = 'https://example.com/1.jpg'
	
	def "A valid post request should work with duplicate images or an empty list of images and return 201"() {
		when:
			def placeOffer = fromJson(detailedPlaceOffer(), Map)
			placeOffer.accommodation.pictures = input

			def response = doPost(PLACE_OFFERS_URI, toJson(placeOffer))

		then:
			resultIs(response, CREATED)

		when:
			final postResponsePlaceOffer = fromJson(response, Map)
			String id = getId(postResponsePlaceOffer)

		then:
			id != null
			postResponsePlaceOffer.accommodation.pictures == output

		when:
			response = doGet("$PLACE_OFFERS_URI/$id")
			final getResponsePlaceOffer = fromJson(response, Map)

		then:
			getResponsePlaceOffer.accommodation.pictures == output

		where:
			input                              | output
			null                               | null
			[]                                 | null
			[PICTURE_EXAMPLE, PICTURE_EXAMPLE] | [PICTURE_EXAMPLE]
	}

	def "A post request with a wrong address should respond with status code 400 and a meaningful error message"() {
		given:
			def invalidPlaceOffer = fromJson(detailedPlaceOffer())
			invalidPlaceOffer.accommodation.address.street.name = 'abcde'

		when:
			final response = doPost(PLACE_OFFERS_URI, toJson(invalidPlaceOffer))

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, BAD_REQUEST, 'Accommodation address not found')
	}

	def "A valid post request should use the default currency value 'EUR' if missing and return 201"() {
		when:
			def placeOffer = fromJson(detailedPlaceOffer(), Map)
			placeOffer.accommodation.currency = input

			def response = doPost(PLACE_OFFERS_URI, toJson(placeOffer))

		then:
			resultIs(response, CREATED)

		when:
			final postResponsePlaceOffer = fromJson(response)
			final id = getId(postResponsePlaceOffer)

		then:
			id != null
			postResponsePlaceOffer.accommodation.currency == output

		when:
			response = doGet("$PLACE_OFFERS_URI/$id")
			final getResponsePlaceOffer = fromJson(response)

		then:
			getResponsePlaceOffer.accommodation.currency == output

		where:
			input | output
			null  | Currency.EUR
			'USD' | Currency.USD
	}

	def "A valid post request should use the default smokingAllowed value 'NO' if missing and return 201"() {
		when:
			def placeOffer = fromJson(detailedPlaceOffer(), Map)
			placeOffer.accommodation.hostPreferences.smokingAllowed = input

			def response = doPost(PLACE_OFFERS_URI, toJson(placeOffer))

		then:
			resultIs(response, CREATED)

		when:
			final postResponsePlaceOffer = fromJson(response)
			final id = getId(postResponsePlaceOffer)

		then:
			id != null
			postResponsePlaceOffer.accommodation.hostPreferences.smokingAllowed == output

		when:
			response = doGet("$PLACE_OFFERS_URI/$id")
			final getResponsePlaceOffer = fromJson(response)

		then:
			getResponsePlaceOffer.accommodation.hostPreferences.smokingAllowed == output

		where:
			input           | output
			null            | SmokingAllowed.NO
			'NOT_SPECIFIED' | SmokingAllowed.NOT_SPECIFIED
	}

	def "A valid post request should use the default petsAllowed value 'NO' if missing and return 201"() {
		when:
			def placeOffer = fromJson(detailedPlaceOffer(), Map)
			placeOffer.accommodation.hostPreferences.petsAllowed = input

			def response = doPost(PLACE_OFFERS_URI, toJson(placeOffer))

		then:
			resultIs(response, CREATED)

		when:
			final postResponsePlaceOffer = fromJson(response)
			final id = getId(postResponsePlaceOffer)

		then:
			id != null
			postResponsePlaceOffer.accommodation.hostPreferences.petsAllowed == output

		when:
			response = doGet("$PLACE_OFFERS_URI/$id")
			final getResponsePlaceOffer = fromJson(response)

		then:
			getResponsePlaceOffer.accommodation.hostPreferences.petsAllowed == output

		where:
			input | output
			null  | PetsAllowed.NO
			'YES' | PetsAllowed.YES
	}

	def "A valid post request should evaluate priceInEuro and return 201"() {
		when:
			def placeOffer = fromJson(detailedPlaceOffer(), Map)
			placeOffer.accommodation.currency = input

			def response = doPost(PLACE_OFFERS_URI, toJson(placeOffer))

		then:
			resultIs(response, CREATED)

		when:
			final postResponsePlaceOffer = fromJson(response)
			final id = getId(postResponsePlaceOffer)
			final pricePerNight = (double) placeOffer.accommodation.pricePerNight

		then:
			id != null
			verifyPriceInEuro(postResponsePlaceOffer, input, pricePerNight)
 
		when:
			response = doGet("$PLACE_OFFERS_URI/$id")
			final getResponsePlaceOffer = fromJson(response)

		then:
			verifyPriceInEuro(getResponsePlaceOffer, input, pricePerNight)

		where:
			input | output
			'EUR' | Currency.EUR
			'USD' | Currency.USD
	}

	private void verifyPriceInEuro(placeOffer, input, pricePerNight) {
		placeOffer.accommodation.with {
			if (input.equals('EUR')) {
				assert priceInEuro == pricePerNight
			} else {
				assert priceInEuro != pricePerNight
				assert priceInEuro > 0.0
			}
		}
	}

	def "A valid post request without amenities should work and return 201"() {
		when:
			def placeOffer = fromJson(detailedPlaceOffer())
			placeOffer.accommodation.amenities = input

			def response = doPost(PLACE_OFFERS_URI, toJson(placeOffer))

		then:
			resultIs(response, CREATED)

		when:
			final postResponsePlaceOffer = fromJson(response)
			final id = getId(postResponsePlaceOffer)

		then:
			id != null
			postResponsePlaceOffer.accommodation.amenities == null

		when:
			response = doGet("$PLACE_OFFERS_URI/$id")
			final getResponsePlaceOffer = fromJson(response)

		then:
			getResponsePlaceOffer.accommodation.amenities == null

		where:
			input  | _
			null   | _
			[]     | _
			[null] | _
	}

	def "A valid post request with a price per night set to 0 should work and return 201"() {
		given:
			def placeOffer = fromJson(detailedPlaceOffer())
			placeOffer.accommodation.pricePerNight = 0

		when:
			def response = doPost(PLACE_OFFERS_URI, toJson(placeOffer))

		then:
			resultIs(response, CREATED)

		when:
			final postResponsePlaceOffer = fromJson(response)
			final id = getId(postResponsePlaceOffer)

		then:
			id != null
			postResponsePlaceOffer.accommodation.with {
				pricePerNight == 0
				priceInEuro == 0.0
			}

		when:
			response = doGet("$PLACE_OFFERS_URI/$id")
			final getResponsePlaceOffer = fromJson(response)

		then:
			getResponsePlaceOffer.accommodation.with {
				pricePerNight == 0
				priceInEuro == 0.0
			}
	}

	private static final NOW = LocalDate.now()

	private static final String TODAY = NOW

	private static final String TOMORROW = NOW.plusDays(1)

	private static final String DAY_AFTER_TOMORROW = NOW.plusDays(2)

	def "A valid post request should have the available dates in order without duplicates and return 201"() {
		given:
			def placeOffer = fromJson(detailedPlaceOffer(), Map)
			placeOffer.accommodation.availableDates = [DAY_AFTER_TOMORROW, TODAY, TOMORROW, TOMORROW]

		when:
			def response = doPost(PLACE_OFFERS_URI, toJson(placeOffer))

		then:
			resultIs(response, CREATED)

		when:
			final postResponsePlaceOffer = fromJson(response, Map)
			final id = getId(postResponsePlaceOffer)

		then:
			id != null
			postResponsePlaceOffer.accommodation.availableDates == [TODAY, TOMORROW, DAY_AFTER_TOMORROW]

		when:
			response = doGet("$PLACE_OFFERS_URI/$id")
			final getResponsePlaceOffer = fromJson(response, Map)

		then:
			getResponsePlaceOffer.accommodation.availableDates == [TODAY, TOMORROW, DAY_AFTER_TOMORROW]
	}

	def "A post request with the same accommodation title for the same user should respond with status code 400 and a meaningful error message"() {
		given:
			final placeOffer = detailedPlaceOffer()

		when:
			doPost(PLACE_OFFERS_URI, placeOffer)
			final response = doPost(PLACE_OFFERS_URI, placeOffer)

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, BAD_REQUEST, 'Accommodation title must be unique for the user')
	}

	def "A post request with the same accommodation title for different users should work and return 201"() {
		given:
			def placeOffer = detailedPlaceOffer()

			def (placeOffer1, placeOffer2) = [HOST1, HOST2].collect { userPlaceOffer(placeOffer, it) }

		when:
			final response1 = doPost(PLACE_OFFERS_URI, placeOffer1, APPLICATION_JSON, getToken(HOST1))
			final response2 = doPost(PLACE_OFFERS_URI, placeOffer2, APPLICATION_JSON, getToken(HOST2))

			(placeOffer1, placeOffer2) = [placeOffer1, placeOffer2].collect { fromJson(it) }

		then:
			resultIs(response1, CREATED)
			resultIs(response2, CREATED)
			placeOffer1.userName != placeOffer2.userName
			placeOffer1.accommodation.title == placeOffer2.accommodation.title
	}
}
