package com.github.kat_ka.spend_the_night.conversion;

import com.github.kat_ka.spend_the_night.model.data.UserEntity;
import com.github.kat_ka.spend_the_night.model.item.User;

public final class UserConverter {

	private UserConverter() {}

	public static UserEntity apiToEntity(User user) {
		var userEntity = new UserEntity();
		userEntity.setId(user.getId());
		userEntity.setUserName(user.getUserName());
		userEntity.setPicture(user.getPicture());
		userEntity.setHostSince(user.getHostSince());
		userEntity.setContact(ContactConverter.apiToEntity(user.getContact()));
		return userEntity;
	}

	public static User entityToApi(UserEntity userEntity) {
		var user = new User();
		user.setId(userEntity.getId());
		user.setUserName(userEntity.getUserName());
		user.setPicture(userEntity.getPicture());
		user.setHostSince(userEntity.getHostSince());
		user.setContact(ContactConverter.entityToApi(userEntity.getContact()));
		return user;
	}
}
