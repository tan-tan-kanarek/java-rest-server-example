package com.example.rest.exceptions;

@SuppressWarnings("serial")
public class RestInternalServerException extends RestException {
	private static RestExceptionType INTERNAL_SERVER_ERROR = new RestExceptionType("INTERNAL_SERVER_ERROR", "Internal server error");
	private static boolean debugEnabled = false;
	
	public static void setDebug(boolean enableDebug)
	{
		debugEnabled = enableDebug;
	}
	
	public RestInternalServerException(String message) {
		super(INTERNAL_SERVER_ERROR);
		System.err.println(message);
		if(debugEnabled)
			setMessage(message);
	}

	public RestInternalServerException(Exception e) {
		super(INTERNAL_SERVER_ERROR);
		System.err.println(e);
		if(debugEnabled && e.getMessage() != null)
			setMessage(e.getMessage());
	}

}
