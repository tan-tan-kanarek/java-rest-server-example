package com.example.rest.request;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import com.example.rest.IRestService;
import com.example.rest.RestContext;
import com.example.rest.annotations.RestAction;
import com.example.rest.annotations.RestParameter;
import com.example.rest.exceptions.RestException;
import com.example.rest.exceptions.RestInternalServerException;
import com.example.rest.exceptions.RestRequestException;
import com.example.rest.response.RestResponseSerializer;

public class RestServiceRequest extends RestRequest {

	private IRestService controllerInstance;
	
	private String action;
	
	protected JsonObject data;
	
	public RestServiceRequest(RestResponseSerializer responseSerializer, IRestService controllerInstance, String action, JsonObject data) {
		super(responseSerializer);
		
		this.controllerInstance = controllerInstance;
		this.action = action;
		this.data = data;
	}
	
	protected RestResponseSerializer getResponseSerializer()
	{
		return (RestResponseSerializer) responseSerializer;
	}
	
	public void setContext(RestContext context)
	{
		controllerInstance.setContext(context);
	}

	protected Method getObjectSetter(Class<?> clazz, String name)
	{
		String setterName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
		for(Method method : clazz.getMethods())
		{
			if(method.getName().equals(setterName) && method.getParameterCount() == 1)
				return method;
		}

		return null;
	}

	protected <T> Object parseDefault(String value, Class<T> clazz, Type type)
	{
		if(int.class.isAssignableFrom(clazz) || Integer.class.isAssignableFrom(clazz))
		{
			return Integer.parseInt(value);
		}
		if(long.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz))
		{
			return Long.parseLong(value);
		}
		if(boolean.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz))
		{
			return value.toLowerCase().equals("true") || value.equals("1");
		}
		if(float.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz))
		{
			return Float.parseFloat(value);
		}
		if(double.class.isAssignableFrom(clazz) || Double.class.isAssignableFrom(clazz))
		{
			return Double.parseDouble(value);
		}
		
		return value;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <T> Object buildObject(JsonValue value, Class<T> clazz, Type type) throws RestException
	{
		if(value instanceof JsonNumber)
		{
			JsonNumber number = (JsonNumber) value;
			if(int.class.isAssignableFrom(clazz) || Integer.class.isAssignableFrom(clazz))
			{
				return number.intValue();
			}
			if(long.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz))
			{
				return number.longValue();
			}
			if(boolean.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz))
			{
				return number.intValue() != 0;
			}
			if(float.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz))
			{
				return number.doubleValue();
			}
			if(double.class.isAssignableFrom(clazz) || Double.class.isAssignableFrom(clazz))
			{
				return number.doubleValue();
			}
		}

		if(value instanceof JsonString)
		{
			String string = ((JsonString) value).getString();
			if(boolean.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz))
			{
				return string.toLowerCase().equals("true") || string.equals("1");
			}
			if(String.class.isAssignableFrom(clazz))
			{
				return string;
			}
		}

		if(boolean.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz))
		{
			return value == JsonValue.TRUE;
		}
		
		T object;
		try 
		{
			object = clazz.newInstance();
		} 
		catch (InstantiationException e) 
		{
			if(e.getCause() instanceof RestException)
			{
				throw (RestException) e.getCause();
			}
			else
			{
				throw new RestInternalServerException(e);
			}
		} 
		catch (IllegalAccessException e) 
		{
			throw new RestInternalServerException(e);
		}
		
		if(object instanceof List)
		{
			if(value instanceof JsonArray)
			{
				JsonArray values = (JsonArray) value;
				for(JsonValue val : values)
				{
					Type genericType = null;
					if(type instanceof ParameterizedType)
						genericType = ((ParameterizedType) type).getActualTypeArguments()[0];
					
					((List) object).add(buildObject(val, (Class) genericType, genericType));
				}
				return object;
			}

			if(value instanceof JsonObject)
			{
				JsonObject map = (JsonObject) value;
				for(JsonValue val : map.values())
				{
					Type genericType = null;
					if(type instanceof ParameterizedType)
						genericType = ((ParameterizedType) type).getActualTypeArguments()[0];
					
					((List) object).add(buildObject(val, (Class) genericType, genericType));
				}
				return object;
			}
		}

		if(object instanceof Map)
		{
			JsonObject map = (JsonObject) value;
			for(String key : map.keySet())
			{
				JsonValue val = map.get(key);
				Type genericType = null;
				if(type instanceof ParameterizedType)
					genericType = ((ParameterizedType) type).getActualTypeArguments()[1];
				
				((Map<String, Object>) object).put(key, buildObject(val, (Class) genericType, genericType));
			}
			return object;
		}
		
		JsonObject map = (JsonObject) value;
		for(String key : map.keySet())
		{
			if(map.isNull(key))
				continue;
			
			Method setter = getObjectSetter(clazz, key);
			if(setter == null)
				continue;

			
			Type propertyType = setter.getParameters()[0].getParameterizedType();
			Object propertyValue = buildObject(map.get(key), (Class) propertyType, propertyType);
			
			try 
			{
				setter.invoke(object, propertyValue);
			} 
			catch (InvocationTargetException e) 
			{
				if(e.getTargetException() instanceof RestException)
				{
					throw (RestException) e.getTargetException();
				}
				else
				{
					throw new RestInternalServerException(e);
				}
			} 
			catch (IllegalArgumentException e) 
			{
				throw new RestRequestException(RestRequestException.INVALID_PARAMETER_TYPE, clazz.getSimpleName() + "." + key, ((Class)propertyType).getSimpleName());
			} 
			catch (IllegalAccessException e) 
			{
				System.err.println(e);
			}
		}
		
		return object;
	}
	
	public RestResponseSerializer handleError(RestException e){
		RestResponseSerializer serializer = getResponseSerializer();
		serializer.setError(e);
		return serializer;
	}

	@Override
	public RestResponseSerializer execute()
	{
		Method actionMethod = null;
		
		for(Method method : controllerInstance.getClass().getMethods())
		{
			if(!method.getName().equals(action))
			{
				if(!method.isAnnotationPresent(RestAction.class))
				{
					continue;
				}

				RestAction actionAnnotation = method.getAnnotation(RestAction.class);
				if(!actionAnnotation.name().equals(action))
				{
					continue;
				}
			}

			actionMethod = method;
		}
		
		if(actionMethod == null)
		{
			return handleError(new RestRequestException(RestRequestException.ACTION_NOT_FOUND, IRestService.getName(controllerInstance.getClass()), action));
		}

		List<Object> arguments = new ArrayList<Object>();
		if(actionMethod.getParameters().length > 0)
		{
			for(Parameter parameter : actionMethod.getParameters())
			{
				String name = parameter.getName();
				RestParameter restParameter = null;
				if(parameter.isAnnotationPresent(RestParameter.class))
				{
					restParameter = parameter.getAnnotation(RestParameter.class); 
					name = restParameter.name();
				}
				
				if(!data.containsKey(name))
				{
					if(restParameter != null && !restParameter.required())
					{
						if(!restParameter.defaultValue().equals(RestParameter.NULL))
						{
							Object argument = parseDefault(restParameter.defaultValue(), parameter.getType(), parameter.getParameterizedType());
							arguments.add(argument);
							continue;
						}
						if(!parameter.getType().isPrimitive())
						{
							arguments.add(null);
							continue;
						}
					}
					return handleError(new RestRequestException(RestRequestException.MISSING_PARAMETER, name));
				}
				
				Object argument = null;
				if(!data.isNull(name))
				{
					try {
						argument = buildObject(data.get(name), parameter.getType(), parameter.getParameterizedType());
					} catch (RestException e) {
						return handleError(e);
					}
				}
				arguments.add(argument);
			}
		}
		
		try
		{
			Object response = actionMethod.invoke(controllerInstance, arguments.toArray());
			getResponseSerializer().setResult(response);
		}
		catch(InvocationTargetException e)
		{
			if(e.getTargetException() instanceof RestException)
			{
				return handleError((RestException) e.getTargetException());
			}
			else
			{
				return handleError(new RestInternalServerException(e));
			}
		} 
		catch (IllegalAccessException | IllegalArgumentException e) 
		{
			return handleError(new RestInternalServerException(e));
		} 
		
		return getResponseSerializer();
	}
	
	public IRestService getControllerInstance()
	{
		return controllerInstance;
	}

	public String getAction()
	{
		return action;
	}

	public JsonObject getData()
	{
		return data;
	}

	public void setData(JsonObject data)
	{
		this.data = data;
	}
}
