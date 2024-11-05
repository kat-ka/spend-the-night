package com.github.kat_ka.spend_the_night.model.data;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_entity")
@Data
@NoArgsConstructor
public class UserEntity {

	public UserEntity(String userName) {
		this.userName = userName;
	}

	@Id
	private String id;

	@Column(name = "user_name", unique = true, updatable = false, nullable = false)
	private String userName;

	private String picture;

	@Column(name = "host_since")
	private String hostSince;

	@Embedded
	private ContactEntity contact;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false)
	private List<AccommodationEntity> accommodations;
}