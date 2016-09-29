package com.example.rest.request;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import com.example.rest.RestContext;
import com.example.rest.RestObjectList;
import com.example.rest.annotations.RestProperty;
import com.example.rest.exceptions.RestException;
import com.example.rest.exceptions.RestInternalServerException;
import com.example.rest.exceptions.RestRequestException;
import com.example.rest.response.RestResponseSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class RestMultiRequest extends RestServiceRequest {
	
	private RestContext context;
	
	@SuppressWarnings("serial")
	class WrongNumberOfTokensException extends Exception
	{
		
	}

	public RestMultiRequest(RestResponseSerializer responseSerializer, JsonObject data) {
		super(responseSerializer, null, null, data);
	}
	
	@Override
	public void setContext(RestContext context)
	{
		this.context = context;
	}

	public List<RestServiceRequest> buildRequests() throws InstantiationException, IllegalAccessException, RestException
	{
		List<RestServiceRequest> requests = new ArrayList<RestServiceRequest>();
		
		for(JsonValue requestData : data.values())
		{
			if(requestData instanceof JsonObject)
			{
				JsonObject request = (JsonObject) requestData;
				if(request.containsKey("service") && request.containsKey("action"))
				{
					RestServiceRequest serviceRequest = RestRequestDeserializer.getServiceRequest(((JsonString) request.get("service")).getString(), ((JsonString) request.get("action")).getString(), request);
					serviceRequest.setContext(context);
					requests.add(serviceRequest);
				}
			}
		}
	
		return requests;
	}
	
	@Override
	public RestResponseSerializer execute()
	{
		List<RestServiceRequest> requests;
		try {
			requests = buildRequests();
		} catch (InstantiationException | IllegalAccessException e) {
			return handleError(new RestInternalServerException(e));
		} catch (RestException e) {
			return handleError(e);
		}
		
		RestObjectList<RestResponseSerializer> responses = new RestObjectList<RestResponseSerializer>();
		RestResponseSerializer response;
		for(int index = 0; index < requests.size(); index++)
		{
			RestServiceRequest requet = requests.get(index);
			if(index > 0)
			{
				JsonObject tokenizedData = requet.getData();
				JsonObject data;
				try {
					data = replaceTokens(tokenizedData, responses);
				} catch (RestRequestException e) {
					
					try {
						response = RestRequestDeserializer.getResponseSerializer();
					} catch (RestInternalServerException e1) {
						return handleError(e1);
					}
					
					response.setError(e);
					responses.add(response);
					continue;
				}
				requet.setData(data);
			}

			response = requet.execute();
			responses.add(response);
		}
		
		RestResponseSerializer serializer = getResponseSerializer();
		serializer.setResult(responses);
		return serializer;
	}

	public JsonValue replaceToken(List<String> tokens, Object response) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, WrongNumberOfTokensException, RestInternalServerException
	{
		if(tokens.size() <= 0)
		{
			JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
			String name = "tmp";
			if(response == null)
			{
				objectBuilder.addNull(name);
			}
			else if(response instanceof Integer)
			{
				objectBuilder.add(name, (int) response);
			}
			else if(response instanceof Float)
			{
				objectBuilder.add(name, (float) response);
			}
			else if(response instanceof Double)
			{
				objectBuilder.add(name, (double) response);
			}
			else if(response instanceof Long)
			{
				objectBuilder.add(name, (long) response);
			}
			else if(response instanceof String)
			{
				objectBuilder.add(name, (String) response);
			}
			else
			{
				throw new WrongNumberOfTokensException();
			}
			
			JsonObject jsonObject = objectBuilder.build();
			return jsonObject.get(name);
		}
		
		String token = tokens.remove(0);

		if(response instanceof Map)
		{
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>)response;
			if(map.containsKey(token))
				return replaceToken(tokens, map.get(token));
		}

		if(response instanceof List && token.matches("\\d+"))
		{
			int index = Integer.parseInt(token);
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>)response;
			if(index < list.size())
				return replaceToken(tokens, list.get(index));
		}

		Class<?> clazz = response.getClass();
		for(Method getter : clazz.getMethods())
		{
			if(!getter.getName().startsWith("get") || getter.getParameterCount() > 0 || getter.getDeclaringClass() == Object.class || getter.isAnnotationPresent(JsonIgnore.class))
				continue;
			
			String name = getter.getName().substring(3, 4).toLowerCase() + getter.getName().substring(4);
			if(getter.isAnnotationPresent(RestProperty.class))
			{
				name = getter.getAnnotation(RestProperty.class).name();
			}
			
			if(!name.equals(token))
				continue;
			
			Object value;
			try {
				value = getter.invoke(response);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RestInternalServerException(e);
			}
			
			return replaceToken(tokens, value);
		}
			
		throw new WrongNumberOfTokensException();
	}

	public JsonValue replaceToken(JsonValue value, RestObjectList<RestResponseSerializer> responses) throws RestRequestException
	{
		if(value instanceof JsonObject)
		{
			return replaceTokens((JsonObject) value, responses);
		}

		else if(value instanceof JsonArray)
		{
			return replaceTokens((JsonArray) value, responses);
		}
		
		else if(value instanceof JsonString)
		{
			String token = ((JsonString) value).getString();

			Pattern pattern = Pattern.compile("^\\{results:(\\d+):(.+)\\}$");
			Matcher matcher = pattern.matcher(token);

			if (matcher.find()) {
				int responseIndex = Integer.parseInt(matcher.group(1)) - 1;
				
				if(responseIndex >= responses.size())
				{
					throw new RestRequestException(RestRequestException.INVALID_MULTIREQUEST_TOKEN, token);
				}
				
				List<String> tokens = new LinkedList<String>(Arrays.asList(matcher.group(2).split(":")));
				try 
				{
					return replaceToken(tokens, responses.get(responseIndex).getResult());
				}
				catch(Exception e)
				{
					throw new RestRequestException(RestRequestException.INVALID_MULTIREQUEST_TOKEN, token);
				}
			}
		}
		
		return value;
	}

	/**
	 * @return array
	 */
	public JsonArray replaceTokens(JsonArray JsonArray, RestObjectList<RestResponseSerializer> responses)
	{
		// TODO
		return JsonArray;
	}

	/**
	 * @return array
	 * @throws RestRequestException 
	 */
	public JsonObject replaceTokens(JsonObject tokenizedData, RestObjectList<RestResponseSerializer> responses) throws RestRequestException
	{
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
		for(String key : tokenizedData.keySet())
		{
			objectBuilder.add(key, replaceToken(tokenizedData.get(key), responses));
		}
		
		return objectBuilder.build();
	}
}
