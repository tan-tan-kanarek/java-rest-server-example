package com.example.rest.xml;


import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorValue;

import com.example.model.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = "objectType")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlDiscriminatorValue("User")
public class RestClientUser implements IRestClientResult {
	
	private Integer id;
	
	private String firstName;
	
	private String lastName;

	private Long createdAt;
	
	private Long updatedAt;
	
	private UserStatus status;

	public RestClientUser() {
	}
	
	public RestClientUser(Map<String, Object> map) {
		if(map.containsKey("id"))
			setId(Integer.parseInt((String) map.get("id")));
		if(map.containsKey("createdAt"))
			setCreatedAt(Long.parseLong((String) map.get("createdAt")));
		if(map.containsKey("updatedAt"))
			setUpdatedAt(Long.parseLong((String) map.get("updatedAt")));
		if(map.containsKey("firstName"))
			setFirstName((String) map.get("firstName"));
		if(map.containsKey("lastName"))
			setLastName((String) map.get("lastName"));
		if(map.containsKey("status"))
			setStatus(UserStatus.fromValue(Integer.parseInt((String) map.get("status"))));
	}

	public Integer getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Long createdAt) {
		this.createdAt = createdAt;
	}

	public Long getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Long updatedAt) {
		this.updatedAt = updatedAt;
	}

	public UserStatus getStatus() {
		return status;
	}

	public void setStatus(UserStatus status) {
		this.status = status;
	}
}
