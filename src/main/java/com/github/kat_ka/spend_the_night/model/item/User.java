package com.github.kat_ka.spend_the_night.model.item;

import lombok.Data;

@Data
public class User {

	private String id;

	private String userName;

	private Contact contact;

	private String picture;

	private String hostSince;
}
