package com.example.rest.response;

import java.io.Writer;

import javax.servlet.ServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "xml")
public class RestXmlResponseSerializer extends RestResponseSerializer {

	public RestXmlResponseSerializer()
	{
		super();
		setObjectType("RestResponse");
	}
	
	@Override
	public void serialize(ServletResponse response, Writer writer) throws Exception {
		response.setContentType("application/xml; charset=UTF-8");

		JAXBContext jaxbContext = JAXBContext.newInstance(getUsedClasses());
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		
		// output pretty printed
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		jaxbMarshaller.marshal(this, writer);
	}
}
