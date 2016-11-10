package com.example.rest.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

@XmlAccessorType(XmlAccessType.FIELD)
public class RestClientResponse implements IRestClientResult {

	private RestClientException error;

	public static class ResultAdapter extends XmlAdapter<AdaptedResult, IRestClientResult> {

		public static IRestClientResult getObject(Element node) throws Exception {
			AdaptedResult result = new AdaptedResult(node);
			return getObject(result);
		}
		
		private static IRestClientResult getObject(AdaptedResult result) throws Exception
		{
			if (result.objectType.equals("User")) {
				return new RestClientUser(result.map);
			}

			if (result.objectType.equals("RestResponse")) {
				return new RestClientResponse(result.map);
			}

			if (result.objectType.equals("array")) {
				RestClientObjectList list = new RestClientObjectList();
				for(Object item : result.map.values())
				{
					list.add(getObject((AdaptedResult) item));
				}
				return list;
			}
			
			throw new Exception("Object type [" + result.objectType + "] not handled");
		}
		
		@Override
		public IRestClientResult unmarshal(AdaptedResult result) throws Exception {
			return getObject(result);
		}

		@Override
		public AdaptedResult marshal(IRestClientResult v) throws Exception {
			return null;
		}
	}
	
	public static class AdaptedResult implements IRestClientResult {

	    @XmlElement
	    String objectType;
	    
	    @XmlAnyElement
	    List<Element> value = new ArrayList<Element>();
	    
	    Map<String, Object> map = new HashMap<String, Object>();

	    public AdaptedResult() {
		}

	    public AdaptedResult(Element node) {
        	nodesToMap(node.getChildNodes());
		}

		public boolean isTextNode(Node node) 
	    {
			return node.getChildNodes().getLength() == 1 && node.getFirstChild() instanceof Text;
		}

		public void nodesToMap(List<Element> nodes) 
	    {
	        for(Node node : nodes) 
	        {
	            map.put(node.getLocalName(), isTextNode(node) ? node.getTextContent() : node);
	        }
	    }

	    public void nodesToMap(NodeList nodes) 
	    {
	        for(int i = 0; i < nodes.getLength(); i++) 
	        {
	        	Node node = nodes.item(i);
	        	if(node instanceof Element)
	        	{
		        	if(node.getLocalName().equals("objectType"))
			    		objectType = node.getTextContent();
		        	else
		        		map.put(node.getLocalName(), isTextNode(node) ? node.getTextContent() : node);
	        	}
	        }
	    }
		    
	    public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
	    	if(objectType.equals("array"))
	    	{
	    		int index = 0;
		        for(Element node : value) 
		        {
		            map.put(index + "", new AdaptedResult(node));
		            index++;
		        }
	    	}
	    	else
	    	{
	    		nodesToMap(value);
	    	}
	    }
	}

	@XmlJavaTypeAdapter(ResultAdapter.class)
	protected IRestClientResult result;

	public RestClientResponse() {
	}

	public RestClientResponse(Map<String, Object> map) throws Exception {
		if(map.containsKey("error") && map.get("error") != null)
		{
			Element element = (Element) map.get("error");
			if(element.hasChildNodes())
			{
				AdaptedResult result = new AdaptedResult(element);
				error = new RestClientException(result.map);
			}
		}

		if(map.containsKey("result") && map.get("result") != null)
		{
			Element element = (Element) map.get("result");
			if(element.hasChildNodes())
			{
				result = ResultAdapter.getObject(element);
			}
		}
	}

	public RestClientException getError() {
		return error;
	}

	public void setError(RestClientException error) {
		this.error = error;
	}

	public IRestClientResult getResult() {
		return result;
	}

	public void setResult(IRestClientResult result) {
		this.result = result;
	}
}
