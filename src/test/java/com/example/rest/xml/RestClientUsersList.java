package com.example.rest.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RestClientUsersList extends RestClientObjectsList {
	
	private Objects objects;
	
	public static class Objects
	{
		@XmlElement(name = "item")
		private List<RestClientUser> objects;
	}
	
	public List<RestClientUser> getObjects() {
		return objects.objects;
	}

	public void setObjects(List<RestClientUser> objects) {
		this.objects = new Objects();
		this.objects.objects = objects;
	}
}