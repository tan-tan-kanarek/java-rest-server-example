package com.example.model;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.example.rest.RestObject;
import com.example.rest.annotations.RestProperty;
import com.example.rest.db.IRestDatabaseObject;
import com.fasterxml.jackson.annotation.JsonIgnore;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class User extends RestObject implements IRestDatabaseObject {
	public static String TABLE_NAME = "users";
	
	private Integer id;
	
	private String firstName;
	
	private String lastName;

	private Long createdAt;

	private Long updatedAt;

	private UserStatus status = UserStatus.Active;

	public User(){
		super();
	}
	
	@JsonIgnore
	public String getTableName()
	{
		return TABLE_NAME;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@RestProperty(name = "firstName", column = "first_name")
	public String getFirstName() {
		return firstName;
	}

	@RestProperty(name = "firstName", column = "first_name")
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@RestProperty(name = "lastName", column = "last_name")
	public String getLastName() {
		return lastName;
	}

	@RestProperty(name = "lastName", column = "last_name")
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@RestProperty(name = "createdAt", column = "created_at")
	public Long getCreatedAt() {
		return createdAt;
	}

	@RestProperty(name = "createdAt", column = "created_at")
	public void setCreatedAt(Long createdAt) {
		this.createdAt = createdAt;
	}

	@RestProperty(name = "updatedAt", column = "updated_at")
	public Long getUpdatedAt() {
		return updatedAt;
	}

	@RestProperty(name = "updatedAt", column = "updated_at")
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
