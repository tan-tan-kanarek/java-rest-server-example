package com.example.rest;

import com.example.rest.annotations.RestService;

public interface IRestService {

	public void setContext(RestContext context);
	
	public static String getName(Class<?> controllerClass)
	{
		if(controllerClass.isAnnotationPresent(RestService.class))
		{
			RestService restService = controllerClass.getAnnotation(RestService.class);
			return restService.name();
		}
		
		return controllerClass.getSimpleName();
	}

}
