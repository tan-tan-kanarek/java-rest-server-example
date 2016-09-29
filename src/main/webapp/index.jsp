<%@page import="com.example.rest.response.IRestResponseSerializer"%><%@page import="com.example.rest.request.RestRequestDeserializer"%><%@page import="com.example.rest.request.RestRequest"%><%@page import="com.example.rest.exceptions.RestInternalServerException"%><%@page import="com.example.rest.RestContext"%><%@page import="java.io.StringWriter"%><%@page import="java.io.OutputStream"%><%@page import="java.util.Map"%><%@page import="com.example.rest.IRestService"%><%@page import="com.example.rest.RestServiceManager"%><%

ServletContext servletContext = getServletContext();
//RestDatabase.install();
if(servletContext.getAttribute("ServicesClasses") != null) {
	RestServiceManager.setClasses((Map<String, Class<? extends IRestService>>) servletContext.getAttribute("ServicesClasses"));
}
else {
	String servicesPackages = getInitParameter("ServicesPackages");
	if(servicesPackages != null)
		RestServiceManager.setServicesPackages(servicesPackages);
	
	String services = getInitParameter("Services");
	if(services != null)
		RestServiceManager.setServices(services);

	servletContext.setAttribute("ServicesClasses", RestServiceManager.getClasses());
}

boolean debug = getInitParameter("debug").toLowerCase().equals("true");
RestInternalServerException.setDebug(debug);

RestContext context = new RestContext();
try
{
	RestRequest restRequest = RestRequestDeserializer.deserialize(request);
	restRequest.setContext(context);
	IRestResponseSerializer restResponseSerializer = restRequest.execute();
	if(restResponseSerializer != null) {
		restResponseSerializer.serialize(response, out);
	}
}
catch(Exception e)
{
	System.err.println(e);
	throw e;
}
finally
{
	context.close();
}
%>