package com.example.rest.json;

public class RestClientResponse {

	private RestClientException error;

	public RestClientException getError() {
		return error;
	}

	public void setError(RestClientException error) {
		this.error = error;
	}
}
