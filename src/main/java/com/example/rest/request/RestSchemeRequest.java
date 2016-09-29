package com.example.rest.request;

import com.example.rest.response.RestXmlSchemeSerializer;

public class RestSchemeRequest extends RestRequest {

	public RestSchemeRequest() {
		super(new RestXmlSchemeSerializer());
	}

}
