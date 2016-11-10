package com.example.rest.xml;

import javax.xml.bind.annotation.XmlSeeAlso;

import org.eclipse.persistence.oxm.annotations.XmlDiscriminatorNode;

@XmlDiscriminatorNode("objectType")
@XmlSeeAlso({RestClientUser.class, RestClientResponse.class})
public interface IRestClientResult {
	
}
