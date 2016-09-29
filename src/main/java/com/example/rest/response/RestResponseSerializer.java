package com.example.rest.response;

import java.io.Writer;
import java.util.ArrayList;

import java.util.List;

import javax.servlet.ServletResponse;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.example.rest.RestObject;
import com.example.rest.exceptions.RestException;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class RestResponseSerializer extends RestObject implements IRestResponseSerializer {

	@XmlElement
	protected Object result = null;

	protected RestException error = null;

	protected List<RestException> warnings = new ArrayList<RestException>();
	
	public RestResponseSerializer()
	{
	}
	
	public void setResult(Object result)
	{
		this.result = result;
	}

	public Object getResult()
	{
		return result;
	}
	
	public void setError(RestException error)
	{
		this.error = error;
	}
	
	public RestException getError()
	{
		return error;
	}

	public List<RestException> getWarnings() {
		return warnings;
	}

	public void setWarnings(List<RestException> warnings) {
		this.warnings = warnings;
	}
	
	abstract public void serialize(ServletResponse response, Writer writer) throws Exception;
}