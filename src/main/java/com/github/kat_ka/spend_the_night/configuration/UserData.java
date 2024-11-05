package com.github.kat_ka.spend_the_night.configuration;

import com.github.kat_ka.spend_the_night.model.data.ContactEntity;
import com.github.kat_ka.spend_the_night.model.data.UserEntity;

import java.util.ArrayList;
import java.util.List;

// taken from https://api.randomuser.me
public class UserData {

	protected static List<UserEntity> getUserEntities() {
		List<UserEntity> list = new ArrayList<>();

		var contact = new ContactEntity();
		contact.setEmail("tialda.bons@example.com");
		contact.setPhone("(077) 4832715");
		var user = new UserEntity();
		user.setId("stn-host1");
		user.setUserName("Tialda Bons");
		user.setContact(contact);
		user.setPicture("https://randomuser.me/api/portraits/women/68.jpg");
		user.setHostSince("2023-06-12");
		list.add(user);

		contact = new ContactEntity();
		contact.setEmail("nathan.fortin@example.com");
		contact.setPhone("D01 N74-9348");
		user = new UserEntity();
		user.setId("stn-host2");
		user.setUserName("Nathan Fortin");
		user.setContact(contact);
		user.setPicture("https://randomuser.me/api/portraits/men/4.jpg");
		user.setHostSince("2015-07-07");
		list.add(user);

		contact = new ContactEntity();
		contact.setEmail("mila.anderson@example.com");
		contact.setPhone("(992)-700-6650");
		user = new UserEntity();
		user.setId("stn-host3");
		user.setUserName("Mila Anderson");
		user.setContact(contact);
		user.setPicture("https://randomuser.me/api/portraits/women/55.jpg");
		user.setHostSince("2017-04-14");
		list.add(user);

		contact = new ContactEntity();
		contact.setEmail("eeli.pakkala@example.com");
		contact.setPhone("06-056-998");
		user = new UserEntity();
		user.setId("stn-host4");
		user.setUserName("Eeli Pakkala");
		user.setContact(contact);
		user.setPicture("https://randomuser.me/api/portraits/men/0.jpg");
		user.setHostSince("2021-08-25");
		list.add(user);

		contact = new ContactEntity();
		contact.setEmail("simona.rygg@example.com");
		contact.setPhone("64597602");
		user = new UserEntity();
		user.setId("stn-guest1");
		user.setUserName("Simona Rygg");
		user.setContact(contact);
		user.setPicture("https://randomuser.me/api/portraits/women/86.jpg");
		user.setHostSince("2022-05-14");
		list.add(user);

		contact = new ContactEntity();
		contact.setEmail("nathaniel.armstrong@example.com");
		contact.setPhone("05-3253-4508");
		user = new UserEntity();
		user.setId("stn-user-not-allowed");
		user.setUserName("Nathaniel Armstrong");
		user.setContact(contact);
		user.setPicture("https://randomuser.me/api/portraits/men/13.jpg");
		user.setHostSince("2024-06-08");
		list.add(user);

		contact = new ContactEntity();
		contact.setEmail("louise.alves@example.com");
		contact.setPhone("(31) 7069-1409");
		user = new UserEntity();
		user.setId("stn-user-renamed"); // now: "stn-user-newname"
		user.setUserName("Louise Alves");
		user.setContact(contact);
		user.setPicture("https://randomuser.me/api/portraits/women/89.jpg");
		user.setHostSince("2018-08-06");
		list.add(user);

		contact = new ContactEntity();
		contact.setEmail("elliot.bourgeois@example.com");
		contact.setPhone("02-52-67-41-49");
		user = new UserEntity();
		user.setId("stn-user-deleted");
		user.setUserName("Elliot Bourgeois");
		user.setContact(contact);
		user.setPicture("https://randomuser.me/api/portraits/men/1.jpg");
		user.setHostSince("2019-06-05");
		list.add(user);

		return list;
	}
}
