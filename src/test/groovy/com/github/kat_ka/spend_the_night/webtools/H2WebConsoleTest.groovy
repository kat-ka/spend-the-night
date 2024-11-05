package com.github.kat_ka.spend_the_night.webtools

import static org.springframework.http.HttpStatus.OK

import io.restassured.RestAssured
import io.restassured.path.xml.XmlPath
import io.restassured.path.xml.XmlPath.CompatibilityMode

import spock.lang.*

@Title("H2 database web console test")
class H2WebConsoleTest extends WebToolSpecification {

	def "H2 web console should work"() {
		given:
			final url = "http://localhost:$port/h2-console"

		when:
			final response = RestAssured.get(url)

		then:
			response.statusCode == OK.value

		when:
			final html = response.asString()
			final xmlPath = new XmlPath(CompatibilityMode.HTML, html)

		then:
			xmlPath.getString('html.body.h1') == 'Welcome to H2'
	}
}
