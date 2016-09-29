package com.example.rest;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;

import com.example.rest.annotations.RestDatabaseIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class RestObject {

	private static List<Class<?>> usedClasses = new ArrayList<Class<?>>();

	@XmlAttribute
	private String objectType;
	
	public RestObject()
	{
		objectType = getClass().getSimpleName();
		addUsedClass(getClass());
	}

	@JsonIgnore
	public static Class<?>[] getUsedClasses() {
		return usedClasses.toArray(new Class[usedClasses.size()]);
	}

	protected static void addUsedClass(Class<?> clazz) {
		usedClasses.add(clazz);
	}

	@RestDatabaseIgnore
	public String getObjectType() {
		return objectType;
	}

	@RestDatabaseIgnore
	protected void setObjectType(String objectType) {
		this.objectType= objectType;
	}
}
