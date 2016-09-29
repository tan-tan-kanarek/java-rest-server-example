package com.example.rest.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = "objectType")
public class RestClientUsersList extends RestClientObjectsList {
	
	private List<RestClientUser> objects;
	
	public List<RestClientUser> getObjects() {
		return objects;
	}

	public void setObjects(List<RestClientUser> objects) {
		this.objects = objects;
	}
}