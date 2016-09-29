package com.example.rest;

import java.sql.SQLException;

import com.example.rest.db.RestDatabase;
import com.example.rest.exceptions.RestInternalServerException;

public class RestContext {
	private RestDatabase database;

	public RestContext() throws RestInternalServerException
	{
		database = new RestDatabase();
	}
	
	public RestDatabase getDatabase() {
		return database;
	}

	public void setDatabase(RestDatabase database) {
		this.database = database;
	};

	public void close() throws RestInternalServerException {
		try {
			database.close();
		} catch (SQLException e) {
			throw new RestInternalServerException(e);
		}
	};
}
