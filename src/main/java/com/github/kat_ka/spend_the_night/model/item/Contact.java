package com.github.kat_ka.spend_the_night.model.item;

import jakarta.validation.constraints.Email;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Contact {

	@Email
	private String email;

	private String phone;
}
