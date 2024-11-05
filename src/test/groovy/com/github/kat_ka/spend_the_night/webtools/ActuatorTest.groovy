package com.github.kat_ka.spend_the_night.webtools

import static org.springframework.http.HttpStatus.OK

import io.restassured.RestAssured
import io.restassured.path.json.JsonPath

import spock.lang.*

@Title("Actuator test")
class ActuatorTest extends WebToolSpecification {

	def "Actuator /health endpoint should work"() {
		given:
			final url = "http://localhost:$port/api/actuator/health"

		when:
			final response = RestAssured.get(url)

		then:
			response.statusCode == OK.value

		when:
			final json = response.asString()

		then:
			JsonPath.from(json).get('status')                      == 'UP'
			JsonPath.from(json).get('components.db.status')        == 'UP'
			JsonPath.from(json).get('components.diskSpace.status') == 'UP'
			JsonPath.from(json).get('components.ping.status')      == 'UP'
	}

	def "Actuator /beans endpoint should work"() {
		given:
			final url = "http://localhost:$port/api/actuator/beans"

		when:
			final response = RestAssured.get(url)

		then:
			response.statusCode == OK.value

		when:
			final json = response.asString()

		then:
			(JsonPath.from(json).get('contexts') as String).contains('[beans:')
	}
}
