package com.example.services;

import com.example.model.Pager;
import com.example.model.User;
import com.example.model.UserFilter;
import com.example.model.UsersList;
import com.example.rest.RestBaseService;
import com.example.rest.RestObjectList;
import com.example.rest.annotations.RestAction;
import com.example.rest.annotations.RestParameter;
import com.example.rest.annotations.RestService;
import com.example.rest.db.RestCriterion;
import com.example.rest.exceptions.RestApplicationException;
import com.example.rest.exceptions.RestException;

@RestService(name = "user")
public class UsersService extends RestBaseService {

	public User add(@RestParameter(name = "user") User user) throws RestException
	{
		return db.insert(User.class, user);
	}
	
	@RestAction(thrown = {"OBJECT_NOT_FOUND"})
	public User update(@RestParameter(name = "id") long id, @RestParameter(name = "user") User user) throws RestException
	{
		if(db.get(User.class, User.TABLE_NAME, id) == null)
			throw new RestApplicationException(RestApplicationException.OBJECT_NOT_FOUND, "User", "" + id);
		
		return db.update(User.class, id, user);
	}

	@RestAction(thrown = {"OBJECT_NOT_FOUND"})
	public User get(@RestParameter(name = "id") long id) throws RestException
	{
		User user = db.get(User.class, User.TABLE_NAME, id);
		if(user == null)
			throw new RestApplicationException(RestApplicationException.OBJECT_NOT_FOUND, "User", "" + id);
		
		return user;
	}
	
	public UsersList search(@RestParameter(name = "filter", required = false) UserFilter filter, @RestParameter(name = "pager", required = false) Pager pager) throws RestException
	{
		RestCriterion criterion = null;
		Integer pageSize = 500;
		Integer pageIndex = null;

		if(filter != null)
		{
			criterion = filter.getCriterion();
		}
			
		if(pager != null)
		{
			pageSize = pager.getPageSize();
			pageIndex = pager.getPageIndex();
		}

		UsersList list = new UsersList();
		RestObjectList<User> objects = db.select(User.class, User.TABLE_NAME, criterion, pageSize, pageIndex);
		list.setObjects(objects);
		if(objects.size() < pageSize)
		{
			list.setTotalCount(objects.size());
		}
		else
		{
			list.setTotalCount(db.count(User.TABLE_NAME, criterion));
		}
		
		return list;
	}

	@RestAction(thrown = {"OBJECT_NOT_FOUND"})
	public void delete(@RestParameter(name = "id") long id) throws RestException
	{
		if(db.get(User.class, User.TABLE_NAME, id) == null)
			throw new RestApplicationException(RestApplicationException.OBJECT_NOT_FOUND, "User", "" + id);
		
		db.delete(User.TABLE_NAME, id);
	}
}
