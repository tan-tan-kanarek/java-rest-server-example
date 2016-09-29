package com.example.rest.json;

public class RestClientUserResponse extends RestClientResponse {
	
	private RestClientUser result;

	public RestClientUser getResult() {
		return result;
	}

	public void setResult(RestClientUser result) {
		this.result = result;
	}
}
