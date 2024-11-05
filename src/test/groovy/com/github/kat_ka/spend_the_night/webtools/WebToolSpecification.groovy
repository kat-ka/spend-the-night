package com.github.kat_ka.spend_the_night.webtools

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

import spock.lang.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebToolSpecification extends Specification {

	@LocalServerPort
	int port
}
