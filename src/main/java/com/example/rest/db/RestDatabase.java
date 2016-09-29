package com.example.rest.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.example.rest.RestObjectList;
import com.example.rest.annotations.RestDatabaseIgnore;
import com.example.rest.annotations.RestProperty;
import com.example.rest.exceptions.RestException;
import com.example.rest.exceptions.RestInternalServerException;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class RestDatabase {
	private Connection connection = null;
	private Statement statement = null;

	class RestDatabaseFormater
	{
		public Map<String, String> values = new HashMap<String, String>();
		public List<String> strings = new LinkedList<String>();
		
		public Long execute(String sql) throws RestInternalServerException
		{
			try 
			{
				PreparedStatement stmt = connection.prepareStatement(sql);

				for(int i = 1; i <= strings.size(); i++)
					stmt.setString(i, strings.get(strings.size() - i));

				stmt.execute();
				ResultSet generatedKeys = stmt.getGeneratedKeys();
				if (generatedKeys.next()) {
				    return generatedKeys.getLong(1);
				}
			} catch (SQLException e) {
				throw new RestInternalServerException(e);
			}
			
			return null;
		}
	}
	
	public RestDatabase() throws RestInternalServerException {
		try {
			if(connection != null && !connection.isClosed())
				return;
		} catch (SQLException e) {
			System.err.println(e);
		}
		
		String provider = "org.sqlite.JDBC";
		try {
			Class.forName(provider);
		} catch (ClassNotFoundException e) {
			throw new RestInternalServerException("JDBC provider [" + provider + "] not found");
		}
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:test.db");
			connection.setAutoCommit(true);
			statement = connection.createStatement();
		} catch (SQLException e) {
			throw new RestInternalServerException(e);
		}
	}

	public void install() throws RestInternalServerException {
		String sql = "CREATE TABLE users (id INTEGER PRIMARY KEY, created_at INTEGER, updated_at INTEGER, first_name TEXT, last_name TEXT, email TEXT, status INTEGER)";
		execute(sql);
	}

	public void close() throws SQLException {
		statement.close();
		connection.close();
	}
	
	private Long execute(String sql) throws RestInternalServerException {
		ResultSet generatedKeys;
		
		try {
			statement.execute(sql);
			generatedKeys = statement.getGeneratedKeys();
			if (generatedKeys.next()) {
			    return generatedKeys.getLong(1);
			}
		} catch (SQLException e) {
			throw new RestInternalServerException(e);
		} catch (Exception e) {
			throw new RestInternalServerException(e);
		}

		return null;
	}
	
	public ResultSet query(String sql) throws RestInternalServerException
	{
		ResultSet rs;
		
		try {
			rs = statement.executeQuery(sql);
			return rs;
		} catch (SQLException e) {
			throw new RestInternalServerException(e);
		}
	}
	
	private RestDatabaseFormater toMap(Object object) throws RestException {
		RestDatabaseFormater formater = new RestDatabaseFormater();
		Class<?> type = object.getClass();
		
		for(Method getter : type.getMethods())
		{
			if(!getter.getName().startsWith("get") || getter.getParameterCount() > 0 || getter.getDeclaringClass() == Object.class || getter.isAnnotationPresent(JsonIgnore.class) || getter.isAnnotationPresent(RestDatabaseIgnore.class))
				continue;
			
			String name = getter.getName().substring(3, 4).toLowerCase() + getter.getName().substring(4);
			if(getter.isAnnotationPresent(RestProperty.class))
			{
				RestProperty restProperty = getter.getAnnotation(RestProperty.class);
				if(!restProperty.column().isEmpty())
				{
					name = restProperty.column();
				}
				else
				{
					name = restProperty.name();
				}
			}
			
			Object value;
			try {
				value = getter.invoke(object);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				throw new RestInternalServerException(e);
			} catch (InvocationTargetException e) {
				if(e.getTargetException() instanceof RestException)
				{
					throw (RestException) e.getTargetException();
				}
				
				throw new RestInternalServerException(e);
			}
			
			if(value == null)
				continue;

			Class<?> getterType = getter.getReturnType();
			if(getterType.isEnum())
			{
				value = ((Enum<?>) value).ordinal();
			}
			else if(int.class.isAssignableFrom(getterType) || Integer.class.isAssignableFrom(getterType))
			{
			}
			else if(long.class.isAssignableFrom(getterType) || Long.class.isAssignableFrom(getterType))
			{
			}
			else if(boolean.class.isAssignableFrom(getterType) || Boolean.class.isAssignableFrom(getterType))
			{
			}
			else if(float.class.isAssignableFrom(getterType) || Float.class.isAssignableFrom(getterType))
			{
			}
			else if(double.class.isAssignableFrom(getterType) || Double.class.isAssignableFrom(getterType))
			{
			}
			else if(String.class.isAssignableFrom(getterType))
			{
				formater.strings.add((String) value);
				value = "?";
			}
			else
			{
				formater.strings.add(value.toString());
				value = "?";
			}
			
			if(value != null)
				formater.values.put(name, value.toString());
		}
		
		return formater;
	}
	
	private <T> T toObject(Class<T> type, ResultSet rs) throws RestException {
		T object;
		try {
			object = type.newInstance();
		} catch (InstantiationException e) {
			if(e.getCause() instanceof RestException)
				throw (RestException) e.getCause();
			
			throw new RestInternalServerException(e);
		} catch (IllegalAccessException e) {
			throw new RestInternalServerException(e);
		}
		
		for(Method setter : type.getMethods())
		{
			if(!setter.getName().startsWith("set") || setter.getParameterCount() != 1 || setter.getDeclaringClass() == Object.class)
				continue;
			
			String name = setter.getName().substring(3, 4).toLowerCase() + setter.getName().substring(4);
			if(setter.isAnnotationPresent(RestProperty.class))
			{
				RestProperty restProperty = setter.getAnnotation(RestProperty.class);
				if(!restProperty.column().isEmpty())
				{
					name = restProperty.column();
				}
				else
				{
					name = restProperty.name();
				}
			}
			
			Class<?> setterType = setter.getParameterTypes()[0];
			Object value = null;

			try {
				if(setterType.isEnum())
				{
					int intValue = rs.getInt(name);
					for(Object enumValue : setterType.getEnumConstants())
					{
						if(((Enum<?>)enumValue).ordinal() == intValue)
						{
							value = enumValue;
							break;
						}
					}
				}
				else if(int.class.isAssignableFrom(setterType) || Integer.class.isAssignableFrom(setterType))
				{
					value = rs.getInt(name);
				}
				else if(long.class.isAssignableFrom(setterType) || Long.class.isAssignableFrom(setterType))
				{
					value = rs.getLong(name);
				}
				else if(boolean.class.isAssignableFrom(setterType) || Boolean.class.isAssignableFrom(setterType))
				{
					value = rs.getBoolean(name);
				}
				else if(float.class.isAssignableFrom(setterType) || Float.class.isAssignableFrom(setterType))
				{
					value = rs.getFloat(name);
				}
				else if(double.class.isAssignableFrom(setterType) || Double.class.isAssignableFrom(setterType))
				{
					value = rs.getDouble(name);
				}
				else if(String.class.isAssignableFrom(setterType))
				{
					value = rs.getString(name);
				}
			} catch (SQLException e) {
				throw new RestInternalServerException(e);
			}

			try {
				setter.invoke(object, value);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				System.err.println(e);
				continue;
			} catch (InvocationTargetException e) {
				if(e.getTargetException() instanceof RestException)
					throw (RestException) e.getTargetException();
				
				throw new RestInternalServerException(e);
			}
		}
		
		return object;
	}
	
	public <T> T get(Class<T> type, String table, long id) throws RestException
	{
		ResultSet rs = query("SELECT * FROM " + table + " WHERE id = " + id);
		try 
		{
			if(rs.next())
			{
				return toObject(type, rs);
			}
		} 
		catch (SQLException e) 
		{
			throw new RestInternalServerException(e);
		}
		finally {
			try {
				rs.close();
			} catch (SQLException e) {
				throw new RestInternalServerException(e);
			}
		}
		
		return null;
	}

	public <T extends IRestDatabaseObject> T insert(Class<T> type, T object) throws RestException
	{
		long now = new Date().getTime() / 1000;
		object.setCreatedAt(now);
		object.setUpdatedAt(now);
		return insert(type, object.getTableName(), object);
	}

	public <T> T insert(Class<T> type, String table, T object) throws RestException
	{
		RestDatabaseFormater formater = toMap(object);
		String columns = String.join(", ", formater.values.keySet());
		String valuesString = String.join(", ", formater.values.values());
		
		Long id = formater.execute("INSERT INTO " + table + " (" + columns + ") VALUES (" + valuesString + ")");
		if(id != null && id > 0)
		{
			return get(type, table, id);
		}

		throw new RestInternalServerException("Failed to insert " + type.getSimpleName());
	}

	public <T extends IRestDatabaseObject> T update(Class<T> type, long id, T object) throws RestException
	{
		long now = new Date().getTime() / 1000;
		object.setUpdatedAt(now);
		return update(type, object.getTableName(), id, object);
	}

	public <T> T update(Class<T> type, String table, long id, T object) throws RestException
	{
		RestDatabaseFormater formater = toMap(object);
		List<String> sets = new ArrayList<String>();
		for(String key : formater.values.keySet())
		{
			sets.add(key + " = " + formater.values.get(key));
		}
		String setClause = String.join(", ", sets);

		formater.execute("UPDATE " + table + " SET " + setClause + " WHERE id = " + id);
		return get(type, table, id);
	}
	
	public <T> RestObjectList<T> select(Class<T> type, String table) throws RestException
	{
		return select(type, table, null, null, null);
	}
	
	public <T> RestObjectList<T> select(Class<T> type, String table, RestCriterion criterion) throws RestException
	{
		return select(type, table, criterion, null, null);
	}
	
	public <T> RestObjectList<T> select(Class<T> type, String table, RestCriterion criterion, Integer pageSize, Integer pageIndex) throws RestException
	{
		String whereClause = "";
		if(criterion != null)
		{
			whereClause = " WHERE " + criterion;
		}
		
		if(pageSize != null)
		{
			whereClause += " LIMIT " + pageSize;
			if(pageIndex != null)
				whereClause += " OFFSET " + ((pageIndex - 1) * pageSize);
		}

		ResultSet rs = query("SELECT * FROM " + table + whereClause);
		
		RestObjectList<T> list = new RestObjectList<T>();
		try 
		{
			while(rs.next())
			{
				list.add(toObject(type, rs));
			}
		} 
		catch (SQLException e) 
		{
			throw new RestInternalServerException(e);
		}
		finally {
			try {
				rs.close();
			} catch (SQLException e) {
				throw new RestInternalServerException(e);
			}
		}
		
		return list;
	}
	
	public Integer count(String table, RestCriterion criterion) throws RestInternalServerException
	{
		String whereClause = "";
		if(criterion != null)
		{
			whereClause = " WHERE " + criterion;
		}
		
		String sql = "SELECT count(id) as cnt FROM " + table + whereClause;
		ResultSet rs = query(sql);
		
		try 
		{
			if(rs.next())
			{
				return rs.getInt("cnt");
			}
			throw new RestInternalServerException("No results returned from SQL [" + sql + "]");
		} 
		catch (SQLException e) 
		{
			throw new RestInternalServerException(e);
		}
		finally {
			try {
				rs.close();
			} catch (SQLException e) {
				throw new RestInternalServerException(e);
			}
		}
	}
	
	public void delete(String table, long id) throws RestInternalServerException
	{
		Map<String, Object> where = new HashMap<String, Object>();
		where.put("id", id);
		
		delete(table, where);
	}
	
	public void delete(String table, Map<String, Object> where) throws RestInternalServerException
	{
		String whereClause = "";
		if(where != null && where.size() > 0)
		{
			List<String> wheres = new ArrayList<String>();
			for(String key : where.keySet())
			{
				Object value = where.get(key);
				
				if(value instanceof String)
				{
					wheres.add(key + " = \"" + (String) value + "\"");
				}
				else
				{
					wheres.add(key + " = " + value);
				}
			}
			whereClause = "WHERE " + String.join(" AND ", wheres);
		}
		
		execute("DELETE FROM " + table + " " + whereClause);
	}
}
