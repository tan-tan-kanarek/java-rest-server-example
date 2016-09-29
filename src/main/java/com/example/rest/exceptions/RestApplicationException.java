package com.example.rest.exceptions;

@SuppressWarnings("serial")
public class RestApplicationException extends RestException {
	public static RestExceptionType OBJECT_NOT_FOUND = new RestExceptionType("OBJECT_NOT_FOUND", "@type@ id [@id@] not found", "type", "id");
	
	public RestApplicationException(RestExceptionType type, String ... values) {
		super(type, values);
	}
}
