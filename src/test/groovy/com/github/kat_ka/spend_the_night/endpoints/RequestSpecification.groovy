package com.github.kat_ka.spend_the_night.endpoints

import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.GUEST1
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.HOST1
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.HOST2
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.HOST3
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.HOST4
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.PLACE_OFFERS_URI
import static com.github.kat_ka.spend_the_night.endpoints.PlaceUtil.USER_NOT_ALLOWED

import static java.nio.charset.StandardCharsets.UTF_8

import static org.springframework.http.HttpHeaders.AUTHORIZATION
import static org.springframework.http.HttpHeaders.CONTENT_TYPE
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print

import com.fasterxml.jackson.databind.ObjectMapper

import com.github.kat_ka.spend_the_night.configuration.UserData
import com.github.kat_ka.spend_the_night.model.item.PlaceOffer

import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.message.BasicNameValuePair
import org.apache.hc.core5.http.NameValuePair

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc

import spock.lang.*

@SpringBootTest
@AutoConfigureMockMvc
class RequestSpecification extends Specification {

	@Autowired
	private MockMvc mvc

	@Autowired
	private ObjectMapper objectMapper

	@Value('${custom.auth.server.token.uri}')
	private tokenUri

	private static final CLIENT_ID1 = UserData.userEntities[0].id

	@Value('${custom.auth.server.client.secret.host1}')
	private clientSecret1

	private static final CLIENT_ID2 = UserData.userEntities[1].id

	@Value('${custom.auth.server.client.secret.host2}')
	private clientSecret2

	private static final CLIENT_ID3 = UserData.userEntities[2].id

	@Value('${custom.auth.server.client.secret.host3}')
	private clientSecret3

	private static final CLIENT_ID4 = UserData.userEntities[3].id

	@Value('${custom.auth.server.client.secret.host4}')
	private clientSecret4

	private static final CLIENT_ID5 = UserData.userEntities[4].id

	@Value('${custom.auth.server.client.secret.guest1}')
	private clientSecret5

	private static final CLIENT_ID6 = UserData.userEntities[5].id

	@Value('${custom.auth.server.client.secret.user-not-allowed}')
	private clientSecret6

	@Value('${spring.data.web.pageable.max-page-size}')
	protected maxPageSize

	private basicPlaceOffer

	def basicPlaceOffer() {
		if (basicPlaceOffer == null) {
			basicPlaceOffer = placeOffer(PlaceUtil.BASIC_PLACE_OFFER)
		}
		withUniqueTitle(basicPlaceOffer)
	}

	private detailedPlaceOffer

	def detailedPlaceOffer() {
		if (detailedPlaceOffer == null) {
			detailedPlaceOffer = placeOffer(PlaceUtil.DETAILED_PLACE_OFFER)
		}
		withUniqueTitle(detailedPlaceOffer)
	}

	private placeOffer(placeOffer) {
		placeOffer = withUniqueTitle(placeOffer)
		final responseAccommodation = fromJson(doPost(PLACE_OFFERS_URI, placeOffer)).accommodation

		def placeOfferWithGeneratedValues = fromJson(placeOffer)
		placeOfferWithGeneratedValues.accommodation.with {
			address.latitude  = responseAccommodation.address.latitude
			address.longitude = responseAccommodation.address.longitude
			address.timezone  = responseAccommodation.address.timezone
			priceInEuro       = responseAccommodation.priceInEuro
		}
		toJson(placeOfferWithGeneratedValues)
	}

	private static final CHARACTERS = ('A'..'Z') + ('a'..'z') + ('0'..'9')

	private random() {
		(1..10).collect { CHARACTERS[new Random().nextInt(CHARACTERS.size())] }.join()
	}

	def withUniqueTitle(placeOffer) {
		def placeOfferObject = fromJson(placeOffer)
		placeOfferObject.accommodation.title = "Cozy Apartment in City Center (no. ${random()})"
		toJson(placeOfferObject)
	}

	def authServerResponse(id = CLIENT_ID1, secret = clientSecret1) {
		List<NameValuePair> urlParameters = new ArrayList<>()
		urlParameters.add(new BasicNameValuePair("client_id", id))
		urlParameters.add(new BasicNameValuePair("client_secret", secret))
		urlParameters.add(new BasicNameValuePair("grant_type", "client_credentials"))

		def post = new HttpPost(tokenUri)
		post.addHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
		post.setEntity(new UrlEncodedFormEntity(urlParameters))

		CloseableHttpClient httpClient = HttpClients.createDefault()
		httpClient.execute(post)
	}

	def accessTokenInAllScopes1() {
		fromJson(EntityUtils.toString(authServerResponse().entity), Map).access_token
	}

	def accessTokenInAllScopes2() {
		fromJson(EntityUtils.toString(authServerResponse(CLIENT_ID2, clientSecret2).entity), Map).access_token
	}

	def accessTokenInAllScopes3() {
		fromJson(EntityUtils.toString(authServerResponse(CLIENT_ID3, clientSecret3).entity), Map).access_token
	}

	def accessTokenInAllScopes4() {
		fromJson(EntityUtils.toString(authServerResponse(CLIENT_ID4, clientSecret4).entity), Map).access_token
	}

	def accessTokenInViewScope() {
		fromJson(EntityUtils.toString(authServerResponse(CLIENT_ID5, clientSecret5).entity), Map).access_token
	}

	def accessTokenInNoScope() {
		fromJson(EntityUtils.toString(authServerResponse(CLIENT_ID6, clientSecret6).entity), Map).access_token
	}

	def getToken(user) {
		switch(user) {
			case HOST1            -> accessTokenInAllScopes1()
			case HOST2            -> accessTokenInAllScopes2()
			case HOST3            -> accessTokenInAllScopes3()
			case HOST4            -> accessTokenInAllScopes4()
			case GUEST1           -> accessTokenInViewScope()
			case USER_NOT_ALLOWED -> accessTokenInNoScope()
		}
	}

	def doPost(uri, requestBody, mediaType = APPLICATION_JSON, accessToken = accessTokenInAllScopes1()) {
		MockHttpServletResponse response = mvc
			.perform(post(uri)
				.header(AUTHORIZATION, "Bearer $accessToken")
				.contentType(mediaType)
				.content(requestBody)
			)
			.andDo(print())
			.andReturn()
			.response
		response.setCharacterEncoding(UTF_8.name())
		response
	}

	def doPostWithoutRequestBody(uri, mediaType = APPLICATION_JSON, accessToken = accessTokenInAllScopes1()) {
		mvc
			.perform(post(uri)
				.header(AUTHORIZATION, "Bearer $accessToken")
				.contentType(mediaType)
			)
			.andDo(print())
			.andReturn()
			.response
	}

	def doPostWithAuthHeaderName(uri, name) {
		mvc
			.perform(post(uri)
				.header(name, "Bearer ${accessTokenInAllScopes1()}")
				.contentType(APPLICATION_JSON)
				.content(basicPlaceOffer())
			)
			.andDo(print())
			.andReturn()
			.response
	}

	def doPostWithAuthHeaderValue(uri, value) {
		mvc
			.perform(post(uri)
				.header(AUTHORIZATION, value)
				.contentType(APPLICATION_JSON)
				.content(basicPlaceOffer())
			)
			.andDo(print())
			.andReturn()
			.response
	}

	def doPostWithoutAuthHeader(uri) {
		mvc
			.perform(post(uri)
				.contentType(APPLICATION_JSON)
				.content(basicPlaceOffer())
			)
			.andDo(print())
			.andReturn()
			.response
	}

	def doGet(uri, accessToken = accessTokenInAllScopes1()) {
		MockHttpServletResponse response = mvc
			.perform(get(uri)
				.header(AUTHORIZATION, "Bearer $accessToken")
			)
			.andDo(print())
			.andReturn()
			.response
		response.setCharacterEncoding(UTF_8.name())
		response
	}

	def doGetWith3Params(uri, name1, value1, name2 = ' ', value2 = '', name3 = ' ', value3 = '', accessToken = accessTokenInAllScopes1()) {
		MockHttpServletResponse response = mvc
			.perform(get(uri)
				.header(AUTHORIZATION, "Bearer $accessToken")
				.param(name1, value1)
				.param(name2, value2)
				.param(name3, value3)
			)
			.andDo(print())
			.andReturn()
			.response
		response.setCharacterEncoding(UTF_8.name())
		response
	}

	def doGetWith4Params(uri, name1, value1, name2, value2, name3 = ' ', value3 = '', name4 = ' ', value4 = '', accessToken = accessTokenInAllScopes1()) {
		MockHttpServletResponse response = mvc
			.perform(get(uri)
				.header(AUTHORIZATION, "Bearer $accessToken")
				.param(name1, value1)
				.param(name2, value2)
				.param(name3, value3)
				.param(name4, value4)
			)
			.andDo(print())
			.andReturn()
			.response
		response.setCharacterEncoding(UTF_8.name())
		response
	}

	def doPut(uri, requestBody, mediaType = APPLICATION_JSON, accessToken = accessTokenInAllScopes1()) {
		mvc
			.perform(put(uri)
				.header(AUTHORIZATION, "Bearer $accessToken")
				.contentType(mediaType)
				.content(requestBody)
			)
			.andDo(print())
			.andReturn()
			.response
	}

	def doDelete(uri, accessToken = accessTokenInAllScopes1()) {
		mvc
			.perform(delete(uri)
				.header(AUTHORIZATION, "Bearer $accessToken")
			)
			.andDo(print())
			.andReturn()
			.response
	}

	def fromJson(content, valueType = PlaceOffer) {
		objectMapper.readValue(content, valueType)
	}

	def fromJson(MockHttpServletResponse response, valueType = PlaceOffer) {
		fromJson(response.contentAsString, valueType)
	}

	def toJson(object) {
		objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(object)
	}

	def getId(placeOffer) {
		placeOffer.accommodation.id
	}

	void resultIs(response, httpStatus, mediaType = APPLICATION_JSON_VALUE) {
		with (response) {
			assert status       == httpStatus.value
			assert errorMessage == null
			assert headers[CONTENT_TYPE].value.startsWith(mediaType)
			assert contentType.startsWith(mediaType)
		}
	}

	void resultContentIs(uri, responseContent, httpStatus, expectedMessage = null) {
		with (responseContent) {
			assert status == httpStatus.value
			assert error  == httpStatus.reasonPhrase
			if (expectedMessage != null) {
				assert message == expectedMessage
			}
		}
		def (requestPath, responsePath) = [uri, responseContent.path]*.replaceFirst('/(\\s|%20)+$', '/')
		assert requestPath == responsePath
	}

	void contentSizeIs(response, size) {
		assert response != null
		assert response.content != null
		assert response.content.size() == size
	}
}
