package com.example.rest.json;

import java.util.List;

public class RestClientMultiResponse extends RestClientResponse {

	private List<Object> result;
	
	public List<Object> getResult() {
		return result;
	}

	public void setResult(List<Object> result) {
		this.result = result;
	}
}
