package com.github.kat_ka.spend_the_night.endpoints

import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.HOST1
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.HOST2
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.HOST3
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.PAGE
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.PLACE_OFFERS_URI
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.SIZE
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.USER_NAME

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON

import com.github.kat_ka.spend_the_night.model.item.Currency

import spock.lang.*

@Title("Test the REST API endpoint /place_offers with the request parameter userName for the GET method")
@Stepwise
class PlaceOffersGetRequestTest extends RequestSpecification {

	def "A get request with an userName request parameter should return the matching PlaceOffers along with status code 200"() {
		given:
			String userA = HOST1
			String userB = HOST2
			String userC = HOST3

			def (placeOfferA, placeOfferB) = [userA, userB].collect { detailedPlaceOffer(it) }

		when:
			String id1 = getId(fromJson(doPost(PLACE_OFFERS_URI, placeOfferA, APPLICATION_JSON, getToken(userA))))
			String id2 = getId(fromJson(doPost(PLACE_OFFERS_URI, withUniqueTitle(placeOfferA), APPLICATION_JSON, getToken(userA))))
			String id3 = getId(fromJson(doPost(PLACE_OFFERS_URI, placeOfferB, APPLICATION_JSON, getToken(userB))))

		and:
			final responseA = doGetWith3Params(PLACE_OFFERS_URI, USER_NAME, userA, SIZE, maxPageSize, ' ', '', getToken(userA))
			final responseB = doGetWith3Params(PLACE_OFFERS_URI, USER_NAME, userB, SIZE, maxPageSize, ' ', '', getToken(userB))
			final responseC = doGetWith3Params(PLACE_OFFERS_URI, USER_NAME, userC, SIZE, maxPageSize, ' ', '', getToken(userC))

		then:
			resultIs(responseA, OK)
			resultIs(responseB, OK)
			resultIs(responseC, OK)

		when:
			final responsePlaceOffersA = fromJson(responseA, Map)
			final responsePlaceOffersB = fromJson(responseB, Map)
			final responsePlaceOffersC = fromJson(responseC, Map)

		then:
			with(responsePlaceOffersA) {
				assert content.any  { getId(it)   == id1   }
				assert content.any  { getId(it)   == id2   }
				content.each { assert getId(it)   != id3   }
				content.each { assert it.userName == userA }
			}
			with(responsePlaceOffersB) {
				content.each { assert getId(it)   != id1   }
				content.each { assert getId(it)   != id2   }
				assert content.any  { getId(it)   == id3   }
				content.each { assert it.userName == userB }
			}
			with(responsePlaceOffersC) {
				if (content.size() > 0) {
					content.each { assert getId(it)   != id1   }
					content.each { assert getId(it)   != id2   }
					content.each { assert getId(it)   != id3   }
					content.each { assert it.userName == userC }
				}
			}
	}

	def "A get request with an empty userName request parameter value should respond with status code 400 and a meaningful error message"() {
		when:
			final response = doGetWith3Params(PLACE_OFFERS_URI, USER_NAME, '  ')

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, BAD_REQUEST, "Required request parameter 'userName' is not present")
	}

	def "A get request with a missing userName request parameter should respond with status code 400 and a meaningful error message"() {
		when:
			final response = doGetWith3Params(PLACE_OFFERS_URI, ' ', '')

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_OFFERS_URI, responseError, BAD_REQUEST, "Required request parameter 'userName' for method parameter type String is not present")
	}

	def "A get request with a size parameter should return the right amount of PlaceOffers along with status code 200"() {
		when:
			def placeOfferD = detailedPlaceOffer(userD)

			final tokenD = getToken(userD)
			(0..1).each { doPost(PLACE_OFFERS_URI, withUniqueTitle(placeOfferD), APPLICATION_JSON, tokenD) }

		and:
			final response = doGetWith3Params(PLACE_OFFERS_URI, USER_NAME, userD, SIZE, requestSize, ' ', '', tokenD)

		then:
			resultIs(response, OK)

		when:
			final responsePlaceOffers = fromJson(response, Map)

		then:
			contentSizeIs(responsePlaceOffers, responseSize)

		where:
			userD | requestSize || responseSize
			HOST1 | '1'         || 1
			HOST2 | '2'         || 2
			HOST3 | '3'         || 2
	}

	def "A get request with a page parameter should return the right amount of PlaceOffers along with status code 200"() {
		when:
			def placeOfferE = detailedPlaceOffer(userE)

			final tokenE = getToken(userE)
			deletePlaceOffersForExampleUser3()
			(0..2).each { doPost(PLACE_OFFERS_URI, withUniqueTitle(placeOfferE), APPLICATION_JSON, tokenE) }

		and:
			final response = doGetWith3Params(PLACE_OFFERS_URI, USER_NAME, userE, SIZE, '2', PAGE, requestPage, tokenE)

		then:
			resultIs(response, OK)

		when:
			final responsePlaceOffers = fromJson(response, Map)

		then:
			contentSizeIs(responsePlaceOffers, responseSize)

		where:
			userE | requestPage || responseSize
			HOST1 | '0'         || 2
			HOST3 | '1'         || 1
			HOST3 | '2'         || 0
	}

	def "A get request with an userName request parameter should return the matching PlaceOffers in the right default order along with status code 200"() {
		given:
			String userF = HOST1

			def (placeOfferF1, placeOfferF2, placeOfferF3, placeOfferF4) = [60, 20, 40, 100].collect {
				def placeOffer = fromJson(detailedPlaceOffer())
				placeOffer.userName = userF
				placeOffer.accommodation.pricePerNight = it
				placeOffer
			}
			placeOfferF4.accommodation.currency = Currency.USD

		when:
			[placeOfferF1, placeOfferF2, placeOfferF3, placeOfferF4].each { doPost(PLACE_OFFERS_URI, toJson(it)) }

		and:
			final response = doGetWith3Params(PLACE_OFFERS_URI, USER_NAME, userF, SIZE, maxPageSize)

		then:
			resultIs(response, OK)

		when:
			final responsePlaceOffers = fromJson(response, Map)

		then:
			with(responsePlaceOffers) {
				(0..<content.size()-1).each { assert content[it].accommodation.priceInEuro <= content[it + 1].accommodation.priceInEuro }

				assert content.any  { it.accommodation.priceInEuro ==  60.0 }
				assert content.any  { it.accommodation.priceInEuro ==  20.0 }
				assert content.any  { it.accommodation.priceInEuro ==  40.0 }
				content.each { assert it.accommodation.priceInEuro != 100.0 }
			}
	}

	def "A get request with an invalid or too high size parameter should use the default size 10 or max size 500 and respond with status code 200"() {
		when:
			def placeOfferG = detailedPlaceOffer(userG)

			def tokenG = getToken(userG)
			(0..responseSize+3).each { doPost(PLACE_OFFERS_URI, withUniqueTitle(placeOfferG), APPLICATION_JSON, tokenG) }

		and:
			final response = doGetWith3Params(PLACE_OFFERS_URI, USER_NAME, userG, SIZE, requestSize, ' ', '', tokenG)

		then:
			resultIs(response, OK)

		when:
			final responsePlaceOffers = fromJson(response, Map)

		then:
			contentSizeIs(responsePlaceOffers, responseSize)

		cleanup:
			deletePlaceOffersForExampleUser3()

		where:
			userG  | requestSize || responseSize
			HOST1  | 'thirty'    || 10
			HOST3  | '600'       || 500
	}

	def "A get request with an invalid page parameter should use the default page 0 and respond with status code 200"() {
		given:
			String userH = HOST3

			def placeOfferH = detailedPlaceOffer(userH)

		when:
			deletePlaceOffersForExampleUser3()
			doPost(PLACE_OFFERS_URI, placeOfferH, APPLICATION_JSON, getToken(userH))

		and:
			final response = doGetWith3Params(PLACE_OFFERS_URI, USER_NAME, userH, SIZE, maxPageSize, PAGE, 'two', getToken(userH))

		then:
			resultIs(response, OK)

		when:
			final responsePlaceOffers = fromJson(response, Map)

		then:
			contentSizeIs(responsePlaceOffers, 1)
	}

	def "A get request with an userName request parameter should work for different placeOffers"() {
		when:
			def placeOfferI = fromJson(basicPlaceOffer())
			placeOfferI.userName = userI
			placeOfferI.accommodation.description = "category: I-$example"
			placeOfferI = toJson(placeOfferI)

			final tokenI = getToken(userI)
			doPost(PLACE_OFFERS_URI, placeOfferI, APPLICATION_JSON, tokenI)

		and:
			final response = doGetWith3Params(PLACE_OFFERS_URI, USER_NAME, userI, SIZE, maxPageSize, ' ', '', tokenI)

		then:
			resultIs(response, OK)

		when:
			def responsePlaceOffers = fromJson(response, Map)
			replaceIdsWithNull(responsePlaceOffers)
			placeOfferI = fromJson(placeOfferI, Map)

		then:
			placeOfferI in responsePlaceOffers.content

		where:
			example | userI
			1       | HOST1
			2       | HOST2
			3       | HOST3
	}
	
	def "A get request with an userName request parameter having leading and trailing spaces should find the user and have the right PlaceOffer in the response"() {
		given:
			String userJ = HOST1

			def placeOfferJ = fromJson(basicPlaceOffer())
			placeOfferJ.accommodation.description = 'category: J'
			placeOfferJ = toJson(placeOfferJ)

		when:
			doPost(PLACE_OFFERS_URI, placeOfferJ)

		and:
			final response = doGetWith3Params(PLACE_OFFERS_URI, USER_NAME, "  $userJ  ", SIZE, maxPageSize)

		then:
			resultIs(response, OK)

		when:
			def responsePlaceOffers = fromJson(response, Map)
			replaceIdsWithNull(responsePlaceOffers)
			placeOfferJ = fromJson(placeOfferJ, Map)

		then:
			placeOfferJ in responsePlaceOffers.content
	}

	private void deletePlaceOffersForExampleUser3() {
		def responseOffers = fromJson(doGetWith3Params(PLACE_OFFERS_URI, USER_NAME, HOST3, SIZE, maxPageSize, ' ', '', getToken(HOST3)), Map)
		responseOffers.content.each { doDelete("$PLACE_OFFERS_URI/${getId(it)}", getToken(HOST3)) }
	}

	private void replaceIdsWithNull(placeOffers) {
		placeOffers.content.each { it.accommodation.id = null }
	}
}
