package com.example.rest.json;

public class RestClientUserListResponse extends RestClientResponse {

	private RestClientUsersList result;
	
	public RestClientUsersList getResult() {
		return result;
	}

	public void setResult(RestClientUsersList result) {
		this.result = result;
	}
}
