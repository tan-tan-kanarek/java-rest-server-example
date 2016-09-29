package com.example.rest.exceptions;

@SuppressWarnings("serial")
public class RestRequestException extends RestException {
	public static RestExceptionType SERVICE_NOT_FOUND = new RestExceptionType("SERVICE_NOT_FOUND", "Service [@service@] not found", "service");
	public static RestExceptionType ACTION_NOT_FOUND = new RestExceptionType("ACTION_NOT_FOUND", "Action [@service@.@action@] not found", "service", "action");
	public static RestExceptionType MISSING_PARAMETER = new RestExceptionType("MISSING_PARAMETER", "Missing parameter [@parameter@]", "parameter");
	public static RestExceptionType INVALID_PARAMETER_TYPE = new RestExceptionType("INVALID_PARAMETER_TYPE", "Invalid parameter [@parameter@] type [@type@] expected", "parameter", "type");
	public static RestExceptionType INVALID_MULTIREQUEST_TOKEN = new RestExceptionType("INVALID_MULTIREQUEST_TOKEN", "Invalid multi-request token [@token@]", "token");
	
	public RestRequestException(RestExceptionType type, String ... values) {
		super(type, values);
	}
}
