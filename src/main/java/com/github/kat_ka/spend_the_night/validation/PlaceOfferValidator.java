package com.github.kat_ka.spend_the_night.validation;

import com.github.kat_ka.spend_the_night.conversion.AddressConverter;
import com.github.kat_ka.spend_the_night.exception.OfferValidationProblem;
import com.github.kat_ka.spend_the_night.model.item.Accommodation;
import com.github.kat_ka.spend_the_night.model.item.Address;
import com.github.kat_ka.spend_the_night.model.item.Currency;
import com.github.kat_ka.spend_the_night.model.item.PlaceOffer;
import com.github.kat_ka.spend_the_night.model.item.external.OsmAddress;
import com.github.kat_ka.spend_the_night.model.item.external.OsmLocation;
import com.github.kat_ka.spend_the_night.model.item.external.Price;
import com.github.kat_ka.spend_the_night.model.item.external.WheretheissTimezone;
import com.github.kat_ka.spend_the_night.normalization.PlaceOfferNormalizer;
import com.github.kat_ka.spend_the_night.repository.AddressRepository;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class PlaceOfferValidator {

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	private WebClient webClient;

	private static final String WHERETHEISS_API_BASE_URL = "https://api.wheretheiss.at/v1/coordinates/";

	private static final String EUR = Currency.EUR.toString();

	public void validatePlaceOffer(PlaceOffer placeOffer) {
		PlaceOfferNormalizer.normalizePlaceOffer(placeOffer);
		validateAccommodation(placeOffer.getAccommodation());
	}

	public static String validateParamNotBlank(String name, String value) {
		if (StringUtils.isBlank(value)) {
			throw new OfferValidationProblem("Required request parameter '" + name + "' is not present");
		}
		return value.trim();
	}

	private void validateAccommodation(Accommodation accommodation) {
		validateAddress(accommodation.getAddress());
		evaluatePriceInEuro(accommodation);
	}

	private void validateAddress(Address address) {
		if (!takeFromDatabase(address)) {
			evaluateLatitudeLongitude(address);
			evaluateTimezone(address);
		}
	}

	private boolean takeFromDatabase(Address address) {
		return addressRepository
				.findFirstByStreet_NameAndStreet_NumberAndCityAndCountryAndPostalCode(
						address.getStreet().getName(),
						address.getStreet().getNumber(),
						address.getCity(),
						address.getCountry(),
						address.getPostalCode())
				.map(addressEntity -> {
					AddressConverter.setLatitudeLongitudeAndTimezone(address, addressEntity);
					return true;
				})
				.orElse(false);
	}

	private void evaluateLatitudeLongitude(Address address) {
		OsmLocation[] response = webClient
				.get()
				.uri(uriBuilder -> uriBuilder
						.scheme("https")
						.host("nominatim.openstreetmap.org")
						.path("/search")
						.queryParam("street", address.getStreet().getNumber() + ", " + address.getStreet().getName())
						.queryParam("city", address.getCity())
						.queryParam("country", address.getCountry())
						.queryParam("postalcode", address.getPostalCode())
						.queryParam("format", "json")
						.queryParam("addressdetails", "1")
						.queryParam("limit", "1")
						.queryParam("polygon_svg", "1")
						.build()
				)
				.retrieve()
				.bodyToMono(OsmLocation[].class)
				.block();
		if (ArrayUtils.isEmpty(response)) {
			throw new OfferValidationProblem("Accommodation address not found");
		}
		useOsmLocation(address, response[0]);
	}

	private static void useOsmLocation(Address address, final OsmLocation osmLocation) {
		OsmAddress osmAddress = osmLocation.getAddress();

		address.getStreet().setName(osmAddress.getRoad());
		address.setCity(osmAddress.getCity());
		address.setPostalCode(osmAddress.getPostcode());
		address.setCountry(osmAddress.getCountry());
		address.setLatitude(osmLocation.getLat());
		address.setLongitude(osmLocation.getLon());
	}

	private void evaluateTimezone(Address address) {
		boolean found = false;
		String url = WHERETHEISS_API_BASE_URL + address.getLatitude() + "," + address.getLongitude();
		WheretheissTimezone response = webClient
				.get()
				.uri(url)
				.retrieve()
				.bodyToMono(WheretheissTimezone.class)
				.block();
		if (response != null) {
			String zoneIdString = response.getTimezoneId();
			if (zoneIdString != null) {
				ZoneId zoneId = null;
				try {
					zoneId = ZoneId.of(zoneIdString);
				} catch (ZoneRulesException e) {
					log.warn("ZoneRulesException for '{}' occured: {}",
							zoneIdString, e.getMessage());
				} catch (DateTimeException e) {
					log.warn("DateTimeException for '{}' occured: {}",
							zoneIdString, e.getMessage());
				}
				if (zoneId != null) {
					address.setTimezone(zoneId);
					found = true;
				}
			}
		}
		if (!found) {
			throw new OfferValidationProblem(
					"Accommodation address validation problem when finding the corresponding timezone");
		}
	}

	private void evaluatePriceInEuro(Accommodation accommodation) {
		boolean found = false;
		if (accommodation.getPricePerNight() == 0 || accommodation.getCurrency() == Currency.EUR) {
			accommodation.setPriceInEuro((double) accommodation.getPricePerNight());
			found = true;
		} else {
			Price response = webClient
					.get()
					.uri(uriBuilder -> uriBuilder
							.scheme("https")
							.host("api.frankfurter.app")
							.path("/latest")
							.queryParam("from", accommodation.getCurrency().toString())
							.queryParam("to", EUR)
							.queryParam("amount", accommodation.getPricePerNight())
							.build()
					)
					.retrieve()
					.bodyToMono(Price.class)
					.block();
			if (response != null) {
				Map<String, Double> rates = response.getRates();
				accommodation.setPriceInEuro(rates.get(EUR));
				found = true;
			}
		}
		if (!found) {
			throw new OfferValidationProblem("Accommodation validation problem for price and currency");
		}
	}
}
