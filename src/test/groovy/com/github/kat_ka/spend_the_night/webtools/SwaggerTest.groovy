package com.github.kat_ka.spend_the_night.webtools

import static org.springframework.http.HttpStatus.OK

import io.restassured.RestAssured
import io.restassured.path.json.JsonPath
import io.restassured.path.xml.XmlPath
import io.restassured.path.xml.XmlPath.CompatibilityMode

import spock.lang.*

@Title("Swagger test")
class SwaggerTest extends WebToolSpecification {

	def "Swagger UI should work for /api/swagger-ui.html"() {
		given:
			final url = "http://localhost:$port/api/swagger-ui.html"

		when:
			final response = RestAssured.get(url)

		then:
			response.statusCode == OK.value

		when:
			final html = response.asString()
			final xmlPath = new XmlPath(CompatibilityMode.HTML, html)

		then:
			xmlPath.getString('html.head.title') == 'Swagger UI'
	}

	def "Swagger UI should work for /api/swagger-ui/index.html"() {
		given:
			final url = "http://localhost:$port/api/swagger-ui/index.html"

		when:
			final response = RestAssured.get(url)

		then:
			response.statusCode == OK.value

		when:
			final html = response.asString()
			final xmlPath = new XmlPath(CompatibilityMode.HTML, html)

		then:
			xmlPath.getString('html.head.title') == 'Swagger UI'
	}

	def "Swagger /api-docs endpoint should work"() {
		given:
			final url = "http://localhost:$port/api/v3/api-docs"

		when:
			final response = RestAssured.get(url)

		then:
			response.statusCode == OK.value

		when:
			final json = response.asString()

		then:
			JsonPath.from(json).get('openapi') == '3.0.1'
	}
}
