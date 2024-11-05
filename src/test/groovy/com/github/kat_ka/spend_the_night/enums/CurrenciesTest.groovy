package com.github.kat_ka.spend_the_night.enums

import com.github.kat_ka.spend_the_night.model.item.Currency as CurrencyEnum

import spock.lang.*

@Title("Currency enum test")
class CurrenciesTest extends Specification {

	def "All currencies in the enum are included in java.util.Currency and the other way around"() {
		given:
			def currencyList = []
			final availableCurrencies = java.util.Currency.getAvailableCurrencies()
			availableCurrencies.each { currencyList << it.getCurrencyCode() }

		and:
			final enumList = CurrencyEnum.values()*.name()

		expect:
			currencyList.each { assert it in enumList }
			enumList.each { assert it in currencyList }
	}
}
