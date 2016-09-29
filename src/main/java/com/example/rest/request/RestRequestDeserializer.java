package com.example.rest.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.servlet.http.HttpServletRequest;

import com.example.rest.IRestService;
import com.example.rest.RestServiceManager;
import com.example.rest.exceptions.RestException;
import com.example.rest.exceptions.RestInternalServerException;
import com.example.rest.exceptions.RestRequestException;
import com.example.rest.response.RestJsonResponseSerializer;
import com.example.rest.response.RestResponseSerializer;
import com.example.rest.response.RestXmlResponseSerializer;

public class RestRequestDeserializer {

	protected static Class<? extends RestResponseSerializer> responseSerializerClass;
	
	public static RestRequest deserialize(HttpServletRequest request) throws Exception
	{
		try
		{
			return parse(request);
		}
		catch (RestException e)
		{
			return getInvalidRequest(e);
		}
	}
	
	public static RestResponseSerializer getResponseSerializer() throws RestInternalServerException
	{
		try {
			return responseSerializerClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RestInternalServerException(e);
		}
	}
	
	public static RestRequest getInvalidRequest(RestException e) throws RestInternalServerException
	{
		return new RestInvalidRequest(getResponseSerializer(), e);
	}
	
	public static RestServiceRequest getServiceRequest(String service, String action, JsonObject data) throws RestException, InstantiationException, IllegalAccessException
	{
		if(service.equals("multirequest"))
		{
			return new RestMultiRequest(getResponseSerializer(), data);
		}
		
		Class<? extends IRestService> controllerClass = RestServiceManager.getServiceClass(service);
		if(controllerClass == null)
		{
			throw new RestRequestException(RestRequestException.SERVICE_NOT_FOUND, service);
		}
		
		IRestService controllerInstance = controllerClass.newInstance();
		
		return new RestServiceRequest(getResponseSerializer(), controllerInstance, action, data);
	}
	
	private static String getRequestBody(HttpServletRequest request) throws IOException
	{
		BufferedReader reader = request.getReader();
		String rawData = "";
		String line = reader.readLine();
		while (line != null){
			rawData += new String(line.getBytes("ISO-8859-1"), "UTF-8");
			rawData += "\n";
			line = reader.readLine();
		}
		reader.reset();
		
		return rawData;
	}
	
	public static Map<String, Object> toMap(JsonObject object) throws JsonException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keySet().iterator();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JsonArray) {
                value = toList((JsonArray) value);
            }

            else if(value instanceof JsonObject) {
                value = toMap((JsonObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JsonArray array) {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.size(); i++) {
            Object value = array.get(i);
            if(value instanceof JsonArray) {
                value = toList((JsonArray) value);
            }

            else if(value instanceof JsonObject) {
                value = toMap((JsonObject) value);
            }
            list.add(value);
        }
        return list;
    }
	
	private static JsonObject parsePath(String path, String queryString)
	{
		JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
		String[] parts;
		
		if(queryString != null) {
			parts = queryString.split("&");
			for(int i = 0; i < parts.length; i++) {
				String[] fieldParts = parts[i].split("=");
				String field = fieldParts[0];
				String value = fieldParts[1];
				
				objectBuilder.add(field, value);
			}
		}

		parts = path.split("/");
		for(int i = 2; i < parts.length; i += 2) {
			String field = parts[i];
			String value = parts[i + 1];
			
			objectBuilder.add(field, value);
		}
		
		return objectBuilder.build();
    }
	
	private static RestRequest parse(HttpServletRequest request) throws RestException, InstantiationException, IllegalAccessException, IOException
	{
		JsonObject data = parsePath(request.getRequestURI(), request.getQueryString());
		if(data.size() == 0 || !data.containsKey("service")) {
			return new RestSchemeRequest();
		}
		
		String service = data.getString("service");
		String action = null;
		if(!service.equals("multirequest"))
		{
			action = data.getString("action");
		}

		JsonObject requestData = null;
		if(request.getContentType() != null)
		{
			String json = null;
			if(request.getContentType().toLowerCase().startsWith("application/json"))
			{
				json = getRequestBody(request);
			}
			else if(request.getContentType().toLowerCase().startsWith("multipart/form-data"))
			{
				@SuppressWarnings("unchecked")
				Enumeration<String> parameters = request.getParameterNames();
				while(parameters.hasMoreElements()){
					String parameter = parameters.nextElement();
					if(parameter.equals("json")) {
						json = new String(request.getParameter(parameter).getBytes("ISO-8859-1"), "UTF-8");
						break;
					}
				}
			}
			
			if(json != null) {
				JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
				
				JsonReader jsonReader = Json.createReader(new StringReader(json));
				if(json.startsWith("["))
				{
					JsonArray jsonArray = jsonReader.readArray();
					int i = 1;
					for(JsonValue value : jsonArray)
					{
						objectBuilder.add(i + "", value);
						i++;
					}
				}
				else 
				{
					JsonObject jsonObject = jsonReader.readObject();
					for(String key : jsonObject.keySet())
					{
						objectBuilder.add(key, jsonObject.get(key));
					}
				}
				jsonReader.close();
				
				for(String key : data.keySet())
				{
					objectBuilder.add(key, data.get(key));
				}
				requestData = objectBuilder.build();
			}
		}
		
		if(requestData == null)
		{
			JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
			
			@SuppressWarnings("unchecked")
			Enumeration<String> parameters = request.getParameterNames();
			while(parameters.hasMoreElements()){
				String parameter = parameters.nextElement();
				String value = new String(request.getParameter(parameter).getBytes("ISO-8859-1"), "UTF-8");
				objectBuilder.add(parameter, value);
			}

			for(String key : data.keySet())
			{
				objectBuilder.add(key, data.get(key));
			}
			requestData = objectBuilder.build();
		}
		
		String accept = request.getHeader("Accept");
		if(accept != null)
			accept = accept.toLowerCase();
		
		if(requestData.containsKey("format") && ((JsonString) requestData.get("format")).getString().toLowerCase().equals("xml")){
			responseSerializerClass = RestXmlResponseSerializer.class;
		}
		else if(accept != null && (accept.startsWith("application/xml") || accept.startsWith("text/xml"))) {
			responseSerializerClass = RestXmlResponseSerializer.class;
		}
		else {
			responseSerializerClass = RestJsonResponseSerializer.class;
		}

		return getServiceRequest(service, action, requestData);
	}
}
