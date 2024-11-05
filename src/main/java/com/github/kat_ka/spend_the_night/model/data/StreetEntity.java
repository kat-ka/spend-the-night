package com.github.kat_ka.spend_the_night.model.data;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreetEntity {

	@Column(name = "street_name")
	private String name;

	@Column(name = "street_number")
	private String number;
}
