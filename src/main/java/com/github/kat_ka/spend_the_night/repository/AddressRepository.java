package com.github.kat_ka.spend_the_night.repository;

import com.github.kat_ka.spend_the_night.model.data.AddressEntity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {

	Optional<AddressEntity> findFirstByStreet_NameAndStreet_NumberAndCityAndCountryAndPostalCode(
			String streetName,
			String streetNumber,
			String city,
			String country,
			String postalCode);
}
