package com.example.rest.response;

import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.json.JsonWriter;
import javax.servlet.ServletResponse;

import com.example.rest.RestObjectList;
import com.example.rest.annotations.RestProperty;
import com.example.rest.exceptions.RestException;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class RestJsonResponseSerializer extends RestResponseSerializer {

	private JsonArray getJsonObject(List<?> list)
	{
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		
		for(Object object : list)
			arrayBuilder.add(getJsonObject(object));
		
		return arrayBuilder.build();
	}

	private JsonObject getJsonObject(Map<String, Object> map)
	{
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
		
		for(String key : map.keySet())
			objectBuilder.add(key, getJsonObject(map.get(key)));
		
		return objectBuilder.build();
	}
	
	private JsonObject getJsonObject(RestException error)
	{
		Map<String, String> parameters = error.getParameters();
		JsonObjectBuilder parametersObjectBuilder = Json.createObjectBuilder();
		for(String parameter : parameters.keySet())
		{
			parametersObjectBuilder.add(parameter, parameters.get(parameter));
		}
		
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
		objectBuilder.add("code", error.getCode());
		objectBuilder.add("message", error.getMessage());
		objectBuilder.add("parameters", parametersObjectBuilder.build());
		return objectBuilder.build();
	}
	
	@SuppressWarnings("unchecked")
	private JsonStructure getJsonObject(Object object)
	{	
		if(object instanceof RestResponseSerializer)
			return getJsonObject((RestResponseSerializer) object);
		
		if(object instanceof List)
			return getJsonObject((List<?>) object);

		if(object instanceof Map)
			return getJsonObject((Map<String, Object>) object);

		if(object instanceof RestObjectList<?>)
			return getJsonObject(((RestObjectList<?>) object).getObjects());
		
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
		
		Class<?> clazz = object.getClass();
		for(Method getter : clazz.getMethods())
		{
			if(!getter.getName().startsWith("get") || getter.getParameterCount() > 0 || getter.getDeclaringClass() == Object.class || getter.isAnnotationPresent(JsonIgnore.class))
				continue;
			
			String name = getter.getName().substring(3, 4).toLowerCase() + getter.getName().substring(4);
			if(getter.isAnnotationPresent(RestProperty.class))
			{
				name = getter.getAnnotation(RestProperty.class).name();
			}
			
			Object value;
			try {
				value = getter.invoke(object);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				System.err.println(e);
				continue;
			}

			if(value == null)
			{
				continue;
			}
			if(value instanceof Enum)
			{
				objectBuilder.add(name, ((Enum<?>) value).ordinal());
			}
			else if(value instanceof Integer)
			{
				objectBuilder.add(name, (int) value);
			}
			else if(value instanceof Float)
			{
				objectBuilder.add(name, (float) value);
			}
			else if(value instanceof Double)
			{
				objectBuilder.add(name, (double) value);
			}
			else if(value instanceof Long)
			{
				objectBuilder.add(name, (long) value);
			}
			else if(value instanceof String)
			{
				objectBuilder.add(name, (String) value);
			}
			else
			{
				objectBuilder.add(name, getJsonObject(value));
			}
		}
		
		return objectBuilder.build();
	}
	
	private JsonObject getJsonObject()
	{
		return getJsonObject(this);
	}
	
	private JsonObject getJsonObject(RestResponseSerializer serializer)
	{
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
		
		if(serializer.getResult() != null)
			objectBuilder.add("result", getJsonObject(serializer.getResult()));

		if(serializer.getError() != null)
			objectBuilder.add("error", getJsonObject(serializer.getError()));

		if(serializer.getWarnings() != null && serializer.getWarnings().size() > 0)
			objectBuilder.add("warnings", getJsonObject(serializer.getWarnings()));
		
		return objectBuilder.build();
	}
	
	@Override
	public void serialize(ServletResponse response, Writer writer) throws Exception {
		response.setContentType("application/json; charset=UTF-8");
		
		JsonWriter jsonWriter = Json.createWriter(writer);
		jsonWriter.write(getJsonObject());
	}

}
