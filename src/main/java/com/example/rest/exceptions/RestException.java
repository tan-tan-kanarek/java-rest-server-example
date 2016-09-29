package com.example.rest.exceptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@SuppressWarnings("serial")
@XmlAccessorType(XmlAccessType.FIELD)
public class RestException extends Exception  {

	private static RestExceptionType DEFAULT = new RestExceptionType("REST_EXCEPTION", "REST Exception");

	public static class Parameter {
	    @XmlAttribute
	    public String key;
	    
	    @XmlValue
	    public String value;
	}

	public static class ParameterWrapper {
	    @XmlElement(name = "item")
	    public Parameter[] parameters;
	}
	
	public static class ParameterXmlAdapter extends XmlAdapter<ParameterWrapper, Map<String, String>> {

		@Override
		public Map<String, String> unmarshal(ParameterWrapper v) throws Exception {
			return null;
		}

		@Override
		public ParameterWrapper marshal(Map<String, String> map) throws Exception {
	        List<Parameter> list = new ArrayList<Parameter>();
	        for (Entry<String, String> entry : map.entrySet()) {
	        	Parameter parameter = new Parameter();
	        	parameter.key = entry.getKey();
	        	parameter.value = entry.getValue();
	            list.add(parameter);
	        }

	        ParameterWrapper wrapper = new ParameterWrapper();
	        wrapper.parameters = list.toArray(new Parameter[list.size()]); 

	        return wrapper;
		}
	}
		
	public static class RestExceptionType {

		public String code;
		
		public String message;
		
		public String[] parameters;
		
		public RestExceptionType(String code, String message, String ... parameters) {
			this.code = code;
			this.message = message;
			this.parameters = parameters;
		}
		
		public String format(String ... values) {
			String formattedMessage = message;
			
			for(int i = 0; i < parameters.length; i++)
			{
				formattedMessage = formattedMessage.replace("@" + parameters[i] + "@", values[i]);
			}
			
			return formattedMessage;
		}
	}

	/**
	 * Needed in order to remove it from the serialized XML
	 */
	public StackTraceElement[] getStackTrace()
	{
		return null;
	}

	private String code;
	
	private String message;

    @XmlJavaTypeAdapter(ParameterXmlAdapter.class)
	private Map<String, String> parameters;

	public RestException()
	{
		this(DEFAULT);
	}
	
	public RestException(RestExceptionType type, String ... values)
	{
		this.code = type.code;
		this.message = type.format(values);
		this.parameters = new HashMap<String, String>();

		for(int i = 0; i < type.parameters.length; i++)
		{
			this.parameters.put(type.parameters[i], values[i]);
		}
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
}
