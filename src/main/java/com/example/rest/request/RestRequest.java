package com.example.rest.request;

import com.example.rest.RestContext;
import com.example.rest.response.IRestResponseSerializer;

public class RestRequest {

	protected IRestResponseSerializer responseSerializer;
	
	public RestRequest(IRestResponseSerializer responseSerializer)
	{
		this.responseSerializer = responseSerializer;
	}
	
	public void setContext(RestContext context)
	{
	}
	
	public IRestResponseSerializer execute(){
		return responseSerializer;
	}
}
