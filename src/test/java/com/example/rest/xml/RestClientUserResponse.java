package com.example.rest.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class RestClientUserResponse extends RestClientResponse {
	
	public RestClientUser getResult() {
		return (RestClientUser) result;
	}

	public void setResult(RestClientUser result) {
		this.result = result;
	}
}
