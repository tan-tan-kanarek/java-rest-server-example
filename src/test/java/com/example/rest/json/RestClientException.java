package com.example.rest.json;

import java.util.Map;

@SuppressWarnings("serial")
public class RestClientException extends Exception {

	private String code;
	
	private String message;
	
	private Map<String, String> parameters;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
}
