package com.example.rest.request;

import com.example.rest.exceptions.RestException;
import com.example.rest.response.RestResponseSerializer;

public class RestInvalidRequest extends RestRequest {

	public RestInvalidRequest(RestResponseSerializer responseSerializer, RestException e) {
		super(responseSerializer);
		responseSerializer.setError(e);
	}
}
