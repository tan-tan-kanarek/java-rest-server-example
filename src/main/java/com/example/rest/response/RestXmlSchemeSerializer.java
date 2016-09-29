package com.example.rest.response;

import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import com.example.rest.IRestService;
import com.example.rest.RestObject;
import com.example.rest.RestObjectList;
import com.example.rest.RestServiceManager;
import com.example.rest.annotations.RestAction;
import com.example.rest.annotations.RestParameter;
import com.example.rest.annotations.RestProperty;
import com.example.rest.annotations.RestType;
import com.example.rest.exceptions.RestApplicationException;
import com.example.rest.exceptions.RestException;
import com.example.rest.exceptions.RestInternalServerException;
import com.example.rest.exceptions.RestRequestException;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class RestXmlSchemeSerializer implements IRestResponseSerializer {

	public static class SchemeNestedType
	{
		@XmlAttribute
		public String type;

		@XmlAttribute
		public String arrayType;

		@XmlAttribute
		public String enumType;
	}

	public static class SchemeArgument extends SchemeNestedType
	{
		@XmlAttribute
		public String type;

		@XmlAttribute
		public String arrayType;

		@XmlAttribute
		public String enumType;
		
		@XmlAttribute
		public String name;

		@XmlAttribute
		public String description;

		@XmlAttribute
		public Integer minLength;

		@XmlAttribute
		public Integer maxLength;

		@XmlAttribute
		public Double minValue;

		@XmlAttribute
		public Double maxValue;
	}

	public static class SchemeActionParam extends SchemeArgument
	{
		@XmlAttribute
		public Boolean optional;

		@XmlAttribute
		public String defaultValue;
	}

	public static class SchemeActionResult extends SchemeNestedType
	{
	}

	public static class SchemeActionThrows
	{
		@XmlAttribute
		public String name;
	}
	
	public static class SchemeAction
	{
		@XmlAttribute
		public String name;

		@XmlAttribute
		public String description;

		@XmlAttribute
		public Boolean enableInMultiRequest = true;

		@XmlElement(name = "param")
		public List<SchemeActionParam> params;

		public SchemeActionResult result;

		@XmlElement(name = "throws")
		public List<SchemeActionThrows> thrown = new ArrayList<SchemeActionThrows>();
	}

	public static class SchemeService
	{
		@XmlAttribute
		public String id;

		@XmlAttribute
		public String name;

		@XmlElement(name = "action")
		public List<SchemeAction> actions;
	}

	public static class SchemeErrorParameter
	{
		@XmlAttribute
		public String name;
	}

	public static class SchemeError
	{
		@XmlAttribute
		public String name;

		@XmlAttribute
		public String code;

		@XmlAttribute
		public String message;

		@XmlElement(name = "parameter")
		public List<SchemeErrorParameter> parameters;
	}

	public static class SchemeEnumValue
	{
		@XmlAttribute
		public String name;

		@XmlAttribute
		public String value;
	}

	public static class SchemeEnum
	{
		@XmlAttribute
		public String name;
		
		@XmlAttribute
		public String enumType;

		@XmlElement(name = "const")
		public List<SchemeEnumValue> values;
	}

	public static class SchemeTypeProperty extends SchemeArgument
	{
		@XmlAttribute
		public Boolean readOnly;
		
		@XmlAttribute
		public Boolean insertOnly;
		
		@XmlAttribute
		public Boolean writeOnly;
	}

	public static class SchemeType
	{
		@XmlAttribute
		public String name;

		@XmlAttribute
		public String base;
		
		@XmlAttribute
		public String description;

		@XmlElement(name = "property")
		public List<SchemeTypeProperty> properties;
	}

	public static class SchemeEnums
	{
		@XmlElement(name = "enum")
		public List<SchemeEnum> enums = new ArrayList<SchemeEnum>();
	}

	public static class SchemeClasses
	{
		@XmlElement(name = "class")
		public List<SchemeType> classes = new ArrayList<SchemeType>();
	}

	public static class SchemeServices
	{
		@XmlElement(name = "service")
		public List<SchemeService> services;
	}

	public static class SchemeErrors
	{
		@XmlElement(name = "error")
		public List<SchemeError> errors;
	}
	
	@XmlRootElement(name = "xml")
	@XmlSeeAlso({SchemeEnum.class, SchemeEnumValue.class, SchemeService.class, SchemeAction.class, SchemeActionParam.class, SchemeActionThrows.class, SchemeActionResult.class})
	public static class Scheme
	{
		@XmlTransient
		private List<Class<?>> loadedClasses = new ArrayList<Class<?>>();

		public SchemeEnums enums = new SchemeEnums();

		public SchemeClasses classes = new SchemeClasses();

		public SchemeServices services = new SchemeServices();

		public SchemeErrors errors = new SchemeErrors();
		
		public Scheme() throws InstantiationException, IllegalAccessException, ClassNotFoundException
		{
			services.services = getServices();
			errors.errors = getErrors();
		}

		private List<SchemeError> getErrors() throws InstantiationException, IllegalAccessException {
			Class<?>[] exceptions = {RestException.class, RestApplicationException.class, RestInternalServerException.class, RestRequestException.class};

			List<SchemeError> errors = new ArrayList<SchemeError>();
			for(Class<?> exception : exceptions)
			{
				for(Field field : exception.getFields())
				{
					if((field.getModifiers() & Modifier.PUBLIC) > 0 && (field.getModifiers() & Modifier.STATIC) > 0 && field.getType() == RestException.RestExceptionType.class)
					{
						RestException.RestExceptionType errorType = (RestException.RestExceptionType) field.get(null);
						SchemeError error = new SchemeError();
						error.name = errorType.code;
						error.code = errorType.code;
						error.message = errorType.message;
						error.parameters = getErrorParameters(errorType.parameters);
						errors.add(error);
					}
				}
			}
			return errors;
		}

		private List<SchemeErrorParameter> getErrorParameters(String[] parameters) {
			List<SchemeErrorParameter> errorParameters = new ArrayList<SchemeErrorParameter>();
			for(String parameter : parameters)
			{
				SchemeErrorParameter errorParameter = new SchemeErrorParameter();
				errorParameter.name = parameter;
				errorParameters.add(errorParameter );
			}
			return errorParameters;
		}

		private List<SchemeService> getServices() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
			List<SchemeService> services = new ArrayList<SchemeService>();
			Map<String, Class<? extends IRestService>> serviceClasses = RestServiceManager.getClasses();
			for(Entry<String, Class<? extends IRestService>> entry : serviceClasses.entrySet())
			{
				SchemeService service = new SchemeService();
				service.id = entry.getKey();
				service.name = entry.getKey();
				service.actions = getActions(entry.getValue());
				services.add(service);
			}
			
			return services;
		}

		private List<SchemeAction> getActions(Class<? extends IRestService> serviceClass) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
			List<SchemeAction> actions = new ArrayList<SchemeAction>();
			for(Method actionMethod : serviceClass.getMethods())
			{
				if(		(actionMethod.getModifiers() & Modifier.PUBLIC) == 0
						||	(actionMethod.getModifiers() & Modifier.STATIC) > 0 
						||	actionMethod.getDeclaringClass() != serviceClass
				)
					continue;
				
				SchemeAction action = new SchemeAction();
				action.name = actionMethod.getName();

				action.params = getActionParams(actionMethod.getParameters());
				if(!actionMethod.getReturnType().getSimpleName().equals("void"))
					action.result = getActionResult(actionMethod.getGenericReturnType());
				
				if(actionMethod.isAnnotationPresent(RestAction.class))
				{
					RestAction annotation = actionMethod.getAnnotation(RestAction.class);
					if(annotation.name().length() > 0)
						action.name = annotation.name();
					
					action.description = annotation.description();
					action.enableInMultiRequest = annotation.enableInMultiRequest();
					action.thrown = getActionThrown(annotation.thrown());
				}
				
				actions.add(action);
			}
			
			return actions;
		}

		private List<SchemeActionThrows> getActionThrown(String[] thrown) {
			List<SchemeActionThrows> list = new ArrayList<SchemeActionThrows>();
			for(String code : thrown)
			{
				SchemeActionThrows item = new SchemeActionThrows();
				item.name = code;
				list.add(item);
			}
			return list;
		}

		private SchemeActionResult getActionResult(Type type) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
			SchemeActionResult result = new SchemeActionResult();
			result.type = getTypeName((Class<?>) type);
			result.arrayType = getArrayType(type);
			result.enumType = getEnumType((Class<?>) type);
			return result;
		}

		private List<SchemeActionParam> getActionParams(Parameter[] parameters) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
			List<SchemeActionParam> params = new ArrayList<SchemeActionParam>();
			for(Parameter parameter : parameters)
			{
				SchemeActionParam param = new SchemeActionParam();
				param.name = parameter.getName();
				param.type = getTypeName(parameter.getType());
				param.arrayType = getArrayType(parameter.getParameterizedType());
				param.enumType = getEnumType(parameter.getType());
				
				if(parameter.isAnnotationPresent(RestParameter.class))
				{
					RestParameter annotation = parameter.getAnnotation(RestParameter.class);
					param.name = annotation.name();
					param.description = annotation.description();
					param.optional = !annotation.required();
					if(!annotation.defaultValue().equals(RestParameter.NULL))
						param.defaultValue = annotation.defaultValue();
				}
				params.add(param );
			}
			return params;
		}

		private String getArrayType(Type type) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
			if(type != null && type instanceof ParameterizedType)
			{
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Class<?> arrayType = null;
				
				if(Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType()))
				{
					Type actualType = parameterizedType.getActualTypeArguments()[1];
					arrayType = Class.forName(actualType.getTypeName());
				}
				
				if(RestObjectList.class.isAssignableFrom((Class<?>) parameterizedType.getRawType()))
				{
					Type actualType = parameterizedType.getActualTypeArguments()[0];
					arrayType = Class.forName(actualType.getTypeName());
				}
				
				if(arrayType != null)
				{
					addType(arrayType);
					return arrayType.getSimpleName();
				}
			}
			
			return null;
		}

		private void addEnum(Enum<?> type) {
			if(!loadedClasses.contains(type.getClass()))
			{
				enums.enums.add(getEnum(type));
				loadedClasses.add(type.getClass());
			}
		}

		private void addType(Class<?> type) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
			if(type == RestObject.class || type == Object.class || type == Number.class || type == Integer.class || type == Long.class || type == Double.class || type == Float.class || type == Boolean.class || type == String.class)
				return;
			
			if(!type.isPrimitive() && !loadedClasses.contains(type))
			{
				loadedClasses.add(type);
				classes.classes.add(getType(type));
			}
		}

		private SchemeType getType(Class<?> type) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
			SchemeType schemeType = new SchemeType();
			schemeType.name = getTypeName(type);
			
			if(type.getSuperclass() != null && type.getSuperclass() != RestObject.class)
				schemeType.base = getTypeName(type.getSuperclass());
			
			if(type.isAnnotationPresent(RestType.class))
			{
				RestType annotation = type.getAnnotation(RestType.class);
				schemeType.name = annotation.name();
				schemeType.description = annotation.description();
			}
			schemeType.properties = getProperties(type);
			
			return schemeType;
		}

		private List<SchemeTypeProperty> getProperties(Class<?> type) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
			List<SchemeTypeProperty> properties = new ArrayList<SchemeTypeProperty>();
			for(Method getter : type.getMethods())
			{
				if(!getter.getName().startsWith("get") || getter.getParameterCount() > 0 || getter.getDeclaringClass() == Object.class || getter.isAnnotationPresent(JsonIgnore.class))
					continue;
				
				SchemeTypeProperty property = new SchemeTypeProperty();
				property.name = getter.getName().substring(3, 4).toLowerCase() + getter.getName().substring(4);

				property.type = getTypeName(getter.getReturnType());
				property.arrayType = getArrayType(getter.getGenericReturnType());
				property.enumType = getEnumType(getter.getReturnType());
				
				if(getter.isAnnotationPresent(RestProperty.class))
				{
					RestProperty annotation = getter.getAnnotation(RestProperty.class);
					property.name = annotation.name();
					property.description = annotation.description();
					property.insertOnly = annotation.insertOnly();
					property.readOnly = annotation.readOnly();
					property.writeOnly = annotation.writeOnly();
					if(annotation.minLength() > Integer.MIN_VALUE)
						property.minLength = annotation.minLength();
					if(annotation.maxLength() < Integer.MAX_VALUE)
						property.maxLength = annotation.maxLength();
					if(annotation.minValue() > Double.MIN_VALUE)
						property.minValue = annotation.minValue();
					if(annotation.maxValue() < Double.MAX_VALUE)
						property.maxValue = annotation.maxValue();
				}
				
				properties.add(property);
			}
			return properties;
		}

		private String getEnumType(Class<?> type) throws InstantiationException, IllegalAccessException {
			if(type.isEnum())
			{
				addEnum((Enum<?>) type.getEnumConstants()[0]);
				return type.getSimpleName();
			}
			
			return null;
		}

		private SchemeEnum getEnum(Enum<?> type) {
			SchemeEnum schemeEnum = new SchemeEnum();
			schemeEnum.name = type.getClass().getSimpleName();
			schemeEnum.enumType = "int";
			schemeEnum.values = getEnumValues(type.getClass());
			return schemeEnum;
		}

		private <T extends Enum<?>> List<SchemeEnumValue> getEnumValues(Class<T> type) {
			List<SchemeEnumValue> list = new ArrayList<SchemeEnumValue>();
			for(T value : type.getEnumConstants())
			{
				SchemeEnumValue enumValue = new SchemeEnumValue();
				enumValue.name = value.name();
				enumValue.value = "" + value.ordinal();
				list.add(enumValue);
			}
			return list;
		}

		private String getTypeName(Class<?> type) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
			if(type.isEnum())
				return "int";

			if(Map.class.isAssignableFrom(type))
				return "map";

			if(RestObjectList.class.isAssignableFrom(type))
				return "array";
			
			addType(type);
			return type.getSimpleName();
		}
	}
	
	@Override
	public void serialize(ServletResponse response, Writer writer) throws Exception {
		response.setContentType("application/xml; charset=UTF-8");

		JAXBContext jaxbContext = JAXBContext.newInstance(Scheme.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		
		// output pretty printed
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		jaxbMarshaller.marshal(new Scheme(), writer);
	}
}
