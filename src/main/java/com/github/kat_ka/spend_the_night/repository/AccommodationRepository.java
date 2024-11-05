package com.github.kat_ka.spend_the_night.repository;

import com.github.kat_ka.spend_the_night.model.data.AccommodationEntity;
import com.github.kat_ka.spend_the_night.model.data.UserEntity;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccommodationRepository extends JpaRepository<AccommodationEntity, UUID> {

	Page<AccommodationEntity> findByUser(UserEntity user, Pageable pageable);

	Page<AccommodationEntity> findByAddress_City(String city, Pageable pageable);
}
