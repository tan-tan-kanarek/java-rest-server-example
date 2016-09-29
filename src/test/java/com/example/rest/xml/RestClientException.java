package com.example.rest.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("serial")
@XmlAccessorType(XmlAccessType.FIELD)
public class RestClientException extends Exception {

	public static class ParametersAdapter extends XmlAdapter<MapElement, Map<String, String>>
	{
		@Override
		public Map<String, String> unmarshal(MapElement v) throws Exception {
	        HashMap<String, String> map = new HashMap<String, String>();
	        for(Element element : v.elements) {
	            map.put(element.getAttribute("key"), element.getTextContent());
	        }
	        return map;
		}

		@Override
		public MapElement marshal(Map<String, String> v) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
	}

    public static class MapElement {        
        @XmlAnyElement
        public List<Element> elements;
    }
	
	private String code;
	
	private String message;

	@XmlJavaTypeAdapter(ParametersAdapter.class)
	private Map<String, String> parameters;

	public RestClientException() {
	}

	public RestClientException(Map<String, Object> map) {
		if(map.containsKey("code"))
			setCode((String) map.get("code"));
		if(map.containsKey("message"))
			setMessage((String) map.get("message"));
		if(map.containsKey("parameters"))
		{
			parameters = new HashMap<String, String>();
			NodeList parametersNode = ((Node) map.get("parameters")).getChildNodes();
			for(int i = 0; i < parametersNode.getLength(); i++)
			{
				if(parametersNode.item(i) instanceof Element)
				{
					Element item = (Element) parametersNode.item(i);
					String key = item.getAttribute("key");
					String value = item.getTextContent();
					parameters.put(key, value);
				}
			}
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
