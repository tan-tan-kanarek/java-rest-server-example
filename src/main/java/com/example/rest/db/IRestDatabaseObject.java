package com.example.rest.db;

public interface IRestDatabaseObject {
	String getTableName();
	void setCreatedAt(Long createdAt);
	void setUpdatedAt(Long updatedAt);
}
