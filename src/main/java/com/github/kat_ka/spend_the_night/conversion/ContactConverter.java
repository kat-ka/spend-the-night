package com.github.kat_ka.spend_the_night.conversion;

import com.github.kat_ka.spend_the_night.model.data.ContactEntity;
import com.github.kat_ka.spend_the_night.model.item.Contact;

public final class ContactConverter {

	private ContactConverter() {}

	protected static ContactEntity apiToEntity(Contact contact) {
		return ContactEntity
					.builder()
					.email(contact.getEmail())
					.phone(contact.getPhone())
					.build();
	}

	protected static Contact entityToApi(ContactEntity contactEntity) {
		return Contact
					.builder()
					.email(contactEntity.getEmail())
					.phone(contactEntity.getPhone())
					.build();
	}
}
