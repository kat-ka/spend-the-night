package com.github.kat_ka.spend_the_night.endpoints

import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.GUEST1
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.HOST1
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.HOST2
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.HOST3
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.HOST4
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.PAGE
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.PLACE_OFFERS_URI
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.PLACE_SEARCH_URI
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.SIZE
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.TOWN
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.USER_NAME
import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON

import com.github.kat_ka.spend_the_night.model.item.Currency

import spock.lang.*

@Title("Test the REST API endpoint /place_search with the request parameters userName and town for the GET method")
class PlaceSearchGetRequestTest extends RequestSpecification {

	def "A get request with userName and town should return the matching PlaceSearchResults along with status code 200"() {
		given:
			String user1 = HOST1
			String user2 = HOST2
			String user3 = GUEST1

			final token1 = getToken(user1)
			final token2 = getToken(user2)
			final token3 = getToken(user3)

			def (placeOfferBerlin,
				 placeOfferHamburg1,
				 placeOfferHamburg2) = (0..2).collect { fromJson(basicPlaceOffer()) }

			placeOfferHamburg1.accommodation.address.with {
				street.name = 'Von-Melle-Park'
				street.number = '1'
				city = 'Hamburg'
				postalCode = '20146'
			}
			placeOfferHamburg2.accommodation.address.with {
				street.name = 'Holstenhofweg'
				street.number = '1'
				city = 'Hamburg'
				postalCode = '22043'
			}
			placeOfferHamburg2.userName = user2

		when:
			String idBerlin   = getId(fromJson(doPost(PLACE_OFFERS_URI, toJson(placeOfferBerlin),   APPLICATION_JSON, token1)))
			String idHamburg1 = getId(fromJson(doPost(PLACE_OFFERS_URI, toJson(placeOfferHamburg1), APPLICATION_JSON, token1)))
			String idHamburg2 = getId(fromJson(doPost(PLACE_OFFERS_URI, toJson(placeOfferHamburg2), APPLICATION_JSON, token2)))

		and:
			final responseA = doGetWith4Params(PLACE_SEARCH_URI, USER_NAME, user1, TOWN, 'Hamburg',   SIZE, maxPageSize, ' ', '', token1)
			final responseB = doGetWith4Params(PLACE_SEARCH_URI, USER_NAME, user1, TOWN, 'Berlin',    SIZE, maxPageSize, ' ', '', token1)
			final responseC = doGetWith4Params(PLACE_SEARCH_URI, USER_NAME, user3, TOWN, 'Hamburg',   SIZE, maxPageSize, ' ', '', token3)
			final responseD = doGetWith4Params(PLACE_SEARCH_URI, USER_NAME, user3, TOWN, 'Berlin',    SIZE, maxPageSize, ' ', '', token3)
			final responseE = doGetWith4Params(PLACE_SEARCH_URI, USER_NAME, user3, TOWN, 'Altenberg', SIZE, maxPageSize, ' ', '', token3)

		then:
			[responseA,
			 responseB,
			 responseC,
			 responseD,
			 responseE].each { resultIs(it, OK) }

		when:
			final searchResultsA = fromJson(responseA, Map)
			final searchResultsB = fromJson(responseB, Map)
			final searchResultsC = fromJson(responseC, Map)
			final searchResultsD = fromJson(responseD, Map)
			final searchResultsE = fromJson(responseE, Map)

		then:
			verifySearchResultsHamburg(searchResultsA, idBerlin, idHamburg1, idHamburg2, user1, user2)
			verifySearchResultsBerlin( searchResultsB, idBerlin, idHamburg1, idHamburg2, user1, user2)
			verifySearchResultsHamburg(searchResultsC, idBerlin, idHamburg1, idHamburg2, user1, user2)
			verifySearchResultsBerlin( searchResultsD, idBerlin, idHamburg1, idHamburg2, user1, user2)

			searchResultsA.content.size() == 2
			searchResultsB.content.size() >  0
			searchResultsC.content.size() == 2
			searchResultsD.content.size() >  0
			searchResultsE.content.size() == 0
	}

	private void verifySearchResultsHamburg(searchResults, idBerlin, idHamburg1, idHamburg2, user1, user2) {
		with(searchResults) {
			assert content.any  { getId(it) == idHamburg1 && it.user.userName == user1 }
			assert content.any  { getId(it) == idHamburg2 && it.user.userName == user2 }
			content.each { assert getId(it) != idBerlin }
			content.each { assert it.accommodation.address.city == 'Hamburg' }
		}
		verifyUserData(searchResults)
	}

	private void verifySearchResultsBerlin(searchResults, idBerlin, idHamburg1, idHamburg2, user1, user2) {
		with(searchResults) {
			assert content.any  { getId(it) == idBerlin && it.user.userName == user1 }
			content.each { assert getId(it) != idHamburg1 }
			content.each { assert getId(it) != idHamburg2 }
			content.each { assert it.accommodation.address.city == 'Berlin' }
		}
		verifyUserData(searchResults)
	}

	private void verifyUserData(searchResults) {
		with(searchResults) {
			content.each { assert it.user.id            == null }
			content.each { assert it.user.contact.email != null }
			content.each { assert it.user.hostSince     != null }
		}
	}

	def "A get request with an empty userName or town should respond with status code 400 and a meaningful error message"() {
		when:
			final response = doGetWith4Params(PLACE_SEARCH_URI, USER_NAME, userNameValue, TOWN, townValue)

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_SEARCH_URI, responseError, BAD_REQUEST, expectedMessage)

		where:
			userNameValue | townValue     || expectedMessage
			' '           | 'Ludwigsburg' || "Required request parameter 'userName' is not present"
			HOST1         | ' '           || "Required request parameter 'town' is not present"
	}

	def "A get request with a missing userName or town should respond with status code 400 and a meaningful error message"() {
		when:
			final response = doGetWith4Params(PLACE_SEARCH_URI, userNameParam.name, userNameParam.value, townParam.name, townParam.value)

		then:
			resultIs(response, BAD_REQUEST)

		when:
			final responseError = fromJson(response, Map)

		then:
			resultContentIs(PLACE_SEARCH_URI, responseError, BAD_REQUEST, expectedMessage)

		where:
			userNameParam                   | townParam                          || expectedMessage
			[name: ' ',       value: '']    | [name: TOWN, value: 'Ludwigsburg'] || "Required request parameter 'userName' for method parameter type String is not present"
			[name: USER_NAME, value: HOST1] | [name: ' ',  value: '']            || "Required request parameter 'town' for method parameter type String is not present"
	}

	def "A get request with a size parameter should return the right amount of search results along with status code 200"() {
		given:
			def placeOffer = fromJson(detailedPlaceOffer())
			placeOffer.userName = HOST2
			placeOffer.accommodation.address.with {
				street.name = 'Merianplatz'
				street.number = '1'
				city = 'Dresden'
				postalCode = '01169'
			}

		when:
			(0..1).each { doPost(PLACE_OFFERS_URI, withUniqueTitle(toJson(placeOffer)), APPLICATION_JSON, getToken(HOST2)) }

		and:
			final response1 = doGetWith4Params(PLACE_SEARCH_URI, USER_NAME, HOST2,  TOWN, 'Dresden', SIZE, '1', ' ', '', getToken(HOST2))
			final response2 = doGetWith4Params(PLACE_SEARCH_URI, USER_NAME, GUEST1, TOWN, 'Dresden', SIZE, '2', ' ', '', getToken(GUEST1))
			final response3 = doGetWith4Params(PLACE_SEARCH_URI, USER_NAME, HOST3,  TOWN, 'Dresden', SIZE, '3', ' ', '', getToken(HOST3))

		then:
			[response1,
			 response2,
			 response3].each { resultIs(it, OK) }

		when:
			final searchResults1 = fromJson(response1, Map)
			final searchResults2 = fromJson(response2, Map)
			final searchResults3 = fromJson(response3, Map)

		then:
			contentSizeIs(searchResults1, 1)
			contentSizeIs(searchResults2, 2)
			contentSizeIs(searchResults3, 2)
	}

	def "A get request with a page parameter should return the right amount of search results along with status code 200"() {
		given:
			def placeOffer = fromJson(detailedPlaceOffer())
			placeOffer.userName = HOST1
			placeOffer.accommodation.address.with {
				street.name = 'Rempartstra\u00dfe'
				street.number = '1'
				city = 'Freiburg im Breisgau'
				postalCode = '79098'
		}

		when:
			(0..2).each { doPost(PLACE_OFFERS_URI, withUniqueTitle(toJson(placeOffer)), APPLICATION_JSON, getToken(HOST1)) }

		and:
			final response1 = doGetWith4Params(PLACE_SEARCH_URI, USER_NAME, GUEST1, TOWN, 'Freiburg im Breisgau', SIZE, '2', PAGE, '0', getToken(GUEST1))
			final response2 = doGetWith4Params(PLACE_SEARCH_URI, USER_NAME, HOST3,  TOWN, 'Freiburg im Breisgau', SIZE, '2', PAGE, '1', getToken(HOST3))
			final response3 = doGetWith4Params(PLACE_SEARCH_URI, USER_NAME, HOST1,  TOWN, 'Freiburg im Breisgau', SIZE, '2', PAGE, '2', getToken(HOST1))

		then:
			[response1,
			 response2,
			 response3].each { resultIs(it, OK) }

		when:
			final searchResults1 = fromJson(response1, Map)
			final searchResults2 = fromJson(response2, Map)
			final searchResults3 = fromJson(response3, Map)

		then:
			contentSizeIs(searchResults1, 2)
			contentSizeIs(searchResults2, 1)
			contentSizeIs(searchResults3, 0)
	}

	def "A valid get request should return the matching search results in the right default order along with status code 200"() {
		given:
			def (placeOffer1,
				 placeOffer2,
				 placeOffer3,
				 placeOffer4) = [65, 25, 45, 105].collect {
					def placeOffer = fromJson(detailedPlaceOffer())
					placeOffer.accommodation.pricePerNight = it
					placeOffer
			}
			placeOffer4.accommodation.currency = Currency.USD

		when:
			[placeOffer1,
			 placeOffer2,
			 placeOffer3,
			 placeOffer4].each { doPost(PLACE_OFFERS_URI, toJson(it)) }

		and:
			final response = doGetWith4Params(PLACE_SEARCH_URI, USER_NAME, HOST1, TOWN, 'Berlin', SIZE, maxPageSize)

		then:
			resultIs(response, OK)

		when:
			final searchResults = fromJson(response, Map)

		then:
			with(searchResults) {
				searchResults.content.size() > 0
				(0..<content.size()-1).each { assert content[it].accommodation.priceInEuro <= content[it + 1].accommodation.priceInEuro }
				assert content.any   { it.accommodation.priceInEuro  == 65.0 }
				assert content.any   { it.accommodation.priceInEuro  == 25.0 }
				assert content.any   { it.accommodation.priceInEuro  == 45.0 }
				content.each  { assert it.accommodation.priceInEuro != 105.0 }
			}
	}

	def "A get request with an invalid or too high size parameter should use the default size 10 or max size 500 and respond with status code 200"() {
		given:
			def placeOffer = fromJson(detailedPlaceOffer())
			placeOffer.userName = HOST4

		when:
			(0..500+3).each { doPost(PLACE_OFFERS_URI, withUniqueTitle(toJson(placeOffer)), APPLICATION_JSON, getToken(HOST4)) }

		and:
			final response1 = doGetWith4Params(PLACE_SEARCH_URI, USER_NAME, HOST2,  TOWN, 'Berlin', SIZE, 'thirty', ' ', '', getToken(HOST2))
			final response2 = doGetWith4Params(PLACE_SEARCH_URI, USER_NAME, GUEST1, TOWN, 'Berlin', SIZE, '600',    ' ', '', getToken(GUEST1))

		then:
			resultIs(response1, OK)
			resultIs(response2, OK)

		when:
			final searchResults1 = fromJson(response1, Map)
			final searchResults2 = fromJson(response2, Map)

		then:
			contentSizeIs(searchResults1,  10)
			contentSizeIs(searchResults2, 500)
	}

	def "A get request with an invalid page parameter should use the default page 0 and respond with status code 200"() {
		given:
			def placeOffer = fromJson(detailedPlaceOffer())
			placeOffer.accommodation.address.with {
				street.name = 'Wilhelm-Leuschner-Platz'
				street.number = '1'
				city = 'Leipzig'
				postalCode = '04107'
			}

		when:
			doPost(PLACE_OFFERS_URI, toJson(placeOffer))

		and:
			final response = doGetWith4Params(PLACE_SEARCH_URI, USER_NAME, HOST4, TOWN, 'Leipzig', SIZE, maxPageSize, PAGE, 'two', getToken(HOST4))

		then:
			resultIs(response, OK)

		when:
			final searchResults = fromJson(response, Map)

		then:
			contentSizeIs(searchResults, 1)
	}

	def "A get request with userName and town should have the right PlaceSearchResults in the response"() {
		given:
			def placeOffer = fromJson(basicPlaceOffer())
			placeOffer.userName = HOST2
			placeOffer.accommodation.address.with {
				street.name = 'Waldowstra\u00dfe'
				street.number = '1'
				city = 'Berlin'
				postalCode = '13053'
			}
			placeOffer = toJson(placeOffer)

		when:
			doPost(PLACE_OFFERS_URI, placeOffer, APPLICATION_JSON, getToken(HOST2))

		and:
			final response = doGetWith4Params(PLACE_SEARCH_URI, USER_NAME, HOST4, TOWN, 'Berlin', SIZE, maxPageSize, ' ', '', getToken(HOST4))

		then:
			resultIs(response, OK)

		when:
			def searchResults = fromJson(response, Map)
			placeOffer = fromJson(placeOffer, Map)

			def responseAccommodations = searchResults.content*.accommodation
			def requestAccommodation = placeOffer.accommodation

			responseAccommodations.each { replaceGeneratedValuesWithNull(it) }
			replaceGeneratedValuesWithNull(requestAccommodation)

		then:
			requestAccommodation in responseAccommodations
	}

	def "A get request with userName and town having leading and trailing spaces should have the right PlaceSearchResults in the response"() {
		given:
			def placeOffer = fromJson(basicPlaceOffer())
			placeOffer.accommodation.address.with {
				street.name = 'Frankfurter Allee'
				street.number = '13'
				city = 'Berlin'
				postalCode = '10247'
			}
			placeOffer = toJson(placeOffer)

		when:
			doPost(PLACE_OFFERS_URI, placeOffer)

		and:
			final response = doGetWith4Params(PLACE_SEARCH_URI, USER_NAME, "  $HOST2  ", TOWN, '  Berlin  ', SIZE, maxPageSize, ' ', '', getToken(HOST2))

		then:
			resultIs(response, OK)

		when:
			def searchResults = fromJson(response, Map)
			placeOffer = fromJson(placeOffer, Map)

			def responseAccommodations = searchResults.content*.accommodation
			def requestAccommodation = placeOffer.accommodation

			responseAccommodations.each { replaceGeneratedValuesWithNull(it) }
			replaceGeneratedValuesWithNull(requestAccommodation)

		then:
			requestAccommodation in responseAccommodations
	}

	private void replaceGeneratedValuesWithNull(accommodation) {
		accommodation.with {
			id = null
			address.latitude = null
			address.longitude = null
			address.timezone = null
			priceInEuro = null
		}
	}
}
