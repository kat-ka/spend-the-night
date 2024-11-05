package com.github.kat_ka.spend_the_night.enums

import com.github.kat_ka.spend_the_night.model.item.Language

import spock.lang.*

@Title("Language enum test")
class LanguagesTest extends Specification {

	def "All languages in the enum are included in java.util.Locale and the other way around"() {
		given:
			def languageList = []
			final isoLanguages = Locale.getISOLanguages()
			isoLanguages.each { languageList << it.toUpperCase() }

		and:
			final enumList = Language.values()*.name()

		expect:
			languageList.each { assert it in enumList }
			enumList.each { assert it in languageList }
	}
}
