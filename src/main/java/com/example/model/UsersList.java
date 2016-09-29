package com.example.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.example.rest.RestObjectList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UsersList extends ObjectsList {
	private RestObjectList<User> objects;

	public RestObjectList<User> getObjects() {
		return objects;
	}

	public void setObjects(RestObjectList<User> objects) {
		this.objects = objects;
	}
}