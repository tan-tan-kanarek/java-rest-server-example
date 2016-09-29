package com.example.rest;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RestObjectList<T> extends RestObject {

	@XmlElement(name = "item")
	private List<T> objects = new ArrayList<T>();

	public RestObjectList()
	{
		super();
		setObjectType("array");
	}
	
	public List<T> getObjects() {
		return objects;
	}

	public void setObjects(List<T> objects) {
		this.objects = objects;
	}

	public void add(T object) {
		objects.add(object);
	}

	public Integer size() {
		return objects.size();
	}

	public T get(int index) {
		return objects.get(index);
	}
}