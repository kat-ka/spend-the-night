package com.github.kat_ka.spend_the_night.repository;

import com.github.kat_ka.spend_the_night.model.data.UserEntity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

	Optional<UserEntity> findByUserName(String userName);
}