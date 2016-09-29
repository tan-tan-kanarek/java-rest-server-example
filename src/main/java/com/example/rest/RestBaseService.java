package com.example.rest;

import com.example.rest.db.RestDatabase;

public class RestBaseService implements IRestService {
	
	protected RestDatabase db;
	
	@Override
	public void setContext(RestContext context) {
		this.db = context.getDatabase();
	}
}
