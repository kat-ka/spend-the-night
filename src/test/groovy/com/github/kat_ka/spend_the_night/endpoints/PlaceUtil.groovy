package com.github.kat_ka.spend_the_night.endpoints

import com.github.kat_ka.spend_the_night.configuration.UserData

class PlaceUtil {

	protected static final PLACE_OFFERS_URI = '/place_offers'

	protected static final PLACE_SEARCH_URI = '/place_search'

	protected static final USER_NAME = 'userName'

	protected static final TOWN = 'town'

	protected static final SIZE = 'size'

	protected static final PAGE = 'page'

	protected static final HOST1 = UserData.userEntities[0].userName

	protected static final HOST2 = UserData.userEntities[1].userName

	protected static final HOST3 = UserData.userEntities[2].userName

	protected static final HOST4 = UserData.userEntities[3].userName

	protected static final GUEST1 = UserData.userEntities[4].userName

	protected static final USER_NOT_ALLOWED = UserData.userEntities[5].userName

	protected static final USER_RENAMED = UserData.userEntities[6].userName

	protected static final USER_DELETED = UserData.userEntities[7].userName

	protected static final BASIC_PLACE_OFFER = """
		{
			"userName": "$HOST1",
			"accommodation": {
				"title": "Cozy Apartment in City Center",
				"address": {
					"street": {
						"name": "B\u00e4nschstra\u00dfe",
						"number": "1"
					},
					"city": "Berlin",
					"postalCode": "10247",
					"country": "Deutschland"
				},
				"pricePerNight": 50,
				"currency": "EUR",
				"availableDates": ["2025-01-17"],
				"hostPreferences": {
					"maxGuests": 2,
					"checkInTime": "15:00",
					"checkOutTime": "11:00",
					"smokingAllowed": "NO",
					"petsAllowed": "YES",
					"languagesSpoken": ["EN"]
				}
			}
		}
		"""

	protected static final DETAILED_PLACE_OFFER = """
		{
			"userName": "$HOST1",
			"accommodation": {
				"title": "Cozy Apartment in City Center",
				"description": "A comfortable one-bedroom apartment with a beautiful view of the city.",
 				"address": {
					"street": {
						"name": "B\u00e4nschstra\u00dfe",
						"number": "1"
					},
					"city": "Berlin",
					"postalCode": "10247",
					"country": "Deutschland"
				},
				"pictures": [
					"https://example.com/1.jpg",
					"https://example.com/2.jpg"
				],
				"pricePerNight": 50,
				"currency": "EUR",
				"availableDates": [
					"2025-01-17",
					"2025-01-18",
					"2025-01-19"
				],
				"amenities": [
					"WIFI",
					"BBQ_GRILL"
				],
				"hostPreferences": {
					"maxGuests": 2,
					"checkInTime": "15:00",
					"checkOutTime": "11:00",
					"smokingAllowed": "NO",
					"petsAllowed": "YES",
					"languagesSpoken": ["EN", "DE"]
				}
			}
		}
		"""
}
