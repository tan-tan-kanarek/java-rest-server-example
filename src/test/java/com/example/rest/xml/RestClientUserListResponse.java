package com.example.rest.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.example.rest.json.RestClientResponse;

@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class RestClientUserListResponse extends RestClientResponse {

	private RestClientUsersList result;
	
	public RestClientUsersList getResult() {
		return result;
	}

	public void setResult(RestClientUsersList result) {
		this.result = result;
	}
}
