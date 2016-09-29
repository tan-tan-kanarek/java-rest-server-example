package com.exmple.rest.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.Test;

import com.example.model.UserStatus;
import com.example.rest.xml.RestClientException;
import com.example.rest.xml.RestClientMultiResponse;
import com.example.rest.xml.RestClientObjectList;
import com.example.rest.xml.RestClientPager;
import com.example.rest.xml.RestClientResponse;
import com.example.rest.xml.RestClientUser;
import com.example.rest.xml.RestClientUserFilter;
import com.example.rest.xml.RestClientUserListResponse;
import com.example.rest.xml.RestClientUserResponse;
import com.example.rest.xml.RestClientUsersList;

public class UserServiceXml {

	private <T> T sendRequest(String service, String action, Map<String, Object> requestData, Class<T> type)
	{
		String path = "java-rest-server-example/service/" + service;
//		String path = "service/" + service;
		if(action != null)
			path += "/action/" + action;
		
		Client client = JerseyClientBuilder.createClient();
		client.register(JacksonFeature.class);
		WebTarget target = client.target("http://localhost:8089").path(path);
//		WebTarget target = client.target("http://localhost:8090").path(path);
		Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_XML_TYPE);
		T response = invocationBuilder.post(Entity.json(requestData), type);
		return response;
	}
	
	private <T> T sendMultiRequest(Map<String, Object> requestData, Class<T> type)
	{
		return sendRequest("multirequest", null, requestData, type);
	}
	
	private RestClientUser add() {
		
		RestClientUser newUser = new RestClientUser();
		newUser.setFirstName(UUID.randomUUID().toString());
		newUser.setLastName(UUID.randomUUID().toString());
		
		Map<String, Object> requestData = new HashMap<String, Object>();
		requestData.put("user", newUser);
		
		RestClientUserResponse response = sendRequest("user", "add", requestData, RestClientUserResponse.class);

		assertNotNull("user is null", response.getResult());
		assertTrue("Result is User", response.getResult() instanceof RestClientUser);
		RestClientUser addedUser = response.getResult();

		assertNotNull("user id is null", addedUser.getId());
		assertNotNull("User createdAt id is null", addedUser.getCreatedAt());
		assertNotNull("User updatedAt id is null", addedUser.getUpdatedAt());
		assertEquals("Status is active", addedUser.getStatus(), UserStatus.Active);
		assertEquals("First name is the same", addedUser.getFirstName(), newUser.getFirstName());
		assertEquals("Last name is the same", addedUser.getLastName(), newUser.getLastName());
		
		return addedUser;
	}
	
	@Test
	public void testAdd() {
		
		add();
	}

	@Test
	public void testError() {
		
		Map<String, Object> requestData = new HashMap<String, Object>();
		requestData.put("ix", "xyz");

		RestClientUserResponse response = sendRequest("user", "get", requestData, RestClientUserResponse.class);
		
		assertTrue("User is null", response.getResult() == null);
		assertNotNull("Error is null", response.getError());
		assertTrue("Error is RestException", response.getError() instanceof RestClientException);

		assertNotNull("Error code is null", response.getError().getCode());
		assertEquals("Error code is wrong", "MISSING_PARAMETER", response.getError().getCode());
	}

	@Test
	public void testGet() {
		
		RestClientUser newUser = add();
		Map<String, Object> requestData = new HashMap<String, Object>();
		requestData.put("id", newUser.getId());

		RestClientUserResponse response = sendRequest("user", "get", requestData, RestClientUserResponse.class);
		
		assertNotNull("user is null", response.getResult());
		assertTrue("Result is User", response.getResult() instanceof RestClientUser);
		RestClientUser addedUser = response.getResult();

		assertNotNull("user id is null", addedUser.getId());
		assertEquals("Status is active", addedUser.getStatus(), UserStatus.Active);
		assertEquals("first name is the same", addedUser.getFirstName(), newUser.getFirstName());
		assertEquals("last name is the same", addedUser.getLastName(), newUser.getLastName());
	}

	@Test
	public void testDelete() {
		
		RestClientUser newUser = add();
		Map<String, Object> requestData = new HashMap<String, Object>();
		requestData.put("id", newUser.getId());

		RestClientResponse response = sendRequest("user", "delete", requestData, RestClientUserResponse.class);

		response = sendRequest("user", "get", requestData, RestClientUserResponse.class);
		assertNotNull("Error is null", response.getError());
		assertTrue("Error is RestException", response.getError() instanceof RestClientException);

		assertNotNull("Error code is null", response.getError().getCode());
		assertEquals("Error code is wrong", "OBJECT_NOT_FOUND", response.getError().getCode());
		assertEquals("Error type parameter is wrong", "User", response.getError().getParameters().get("type"));
		assertEquals("Error type parameter is wrong", newUser.getId().intValue(), Integer.parseInt(response.getError().getParameters().get("id")));
	}

	@Test
	public void testUpdate() {
		
		RestClientUser newUser = add();

		RestClientUser updateUser = new RestClientUser();
		updateUser.setFirstName(UUID.randomUUID().toString());
		
		Map<String, Object> requestData = new HashMap<String, Object>();
		requestData.put("id", newUser.getId());
		requestData.put("user", updateUser);

		RestClientUserResponse response = sendRequest("user", "update", requestData, RestClientUserResponse.class);
		
		assertNotNull("user is null", response.getResult());
		assertTrue("Result is User", response.getResult() instanceof RestClientUser);
		RestClientUser updatedUser = response.getResult();

		assertNotNull("user id is null", updatedUser.getId());
		assertEquals("Status is active", updatedUser.getStatus(), UserStatus.Active);
		assertEquals("first name is the same", updatedUser.getFirstName(), updateUser.getFirstName());
		assertEquals("last name is the same", updatedUser.getLastName(), newUser.getLastName());
	}

	@Test
	public void testSearch() throws InterruptedException {
		Thread.sleep(2000);
		long now = new Date().getTime() / 1000;
		
		List<Integer> ids = new ArrayList<Integer>();
		int count = 5;
		
		RestClientUserFilter filter = new RestClientUserFilter();
		filter.setCreatedAtGreaterThanOrEqual(now);
		
		for(int i = 0; i < count; i++)
		{
			RestClientUser newUser = add();
			ids.add(newUser.getId());
		}
		assertEquals(count, ids.size());

		Map<String, Object> requestData = new HashMap<String, Object>();
		requestData.put("filter", filter);
		
		Thread.sleep(2000);

		RestClientUserListResponse response = sendRequest("user", "search", requestData, RestClientUserListResponse.class);

		assertNotNull("User-list is null", response.getResult());
		assertTrue("Result is UserList", response.getResult() instanceof RestClientUsersList);
		RestClientUsersList list = response.getResult();
		
		assertEquals(count, (int) list.getTotalCount());
		assertEquals(count, list.getObjects().size());
		for(RestClientUser user : list.getObjects())
		{
			assertTrue(ids.contains(user.getId()));
			assertEquals("Status is active", user.getStatus(), UserStatus.Active);
		}
	}

	private void testPage(int page) throws InterruptedException {
		Thread.sleep(2000);
		long now = new Date().getTime() / 1000;
		
		List<Integer> ids = new ArrayList<Integer>();
		int count = 5;
		
		RestClientPager pager = new RestClientPager();
		pager.setPageSize(2);
		pager.setPageIndex(page);

		RestClientUserFilter filter = new RestClientUserFilter();
		filter.setCreatedAtGreaterThanOrEqual(now);
		
		for(int i = 0; i < count; i++)
		{
			RestClientUser newUser = add();
			if(i >= (pager.getPageSize() * (pager.getPageIndex() - 1)) && i < (pager.getPageSize() * pager.getPageIndex()))
				ids.add(newUser.getId());
		}
		assertEquals((int) pager.getPageSize(), ids.size());

		Map<String, Object> requestData = new HashMap<String, Object>();
		requestData.put("filter", filter);
		requestData.put("pager", pager);
		
		Thread.sleep(2000);

		RestClientUserListResponse response = sendRequest("user", "search", requestData, RestClientUserListResponse.class);

		assertNotNull("User-list is null", response.getResult());
		assertTrue("Result is UserList", response.getResult() instanceof RestClientUsersList);
		RestClientUsersList list = response.getResult();
		
		assertEquals(count, (int) list.getTotalCount());
		assertEquals((int) pager.getPageSize(), list.getObjects().size());
		for(RestClientUser user : list.getObjects())
		{
			assertTrue(ids.contains(user.getId()));
			assertEquals("Status is active", user.getStatus(), UserStatus.Active);
		}
	}

	@Test
	public void testPage1() throws InterruptedException {
		testPage(1);
	}

	@Test
	public void testPage2() throws InterruptedException {
		testPage(2);
	}

	@Test
	public void testMultiRequest() {

		RestClientUser newUser = new RestClientUser();
		newUser.setFirstName(UUID.randomUUID().toString());
		newUser.setLastName(UUID.randomUUID().toString());

		RestClientUser updateUser = new RestClientUser();
		updateUser.setFirstName(UUID.randomUUID().toString());
		updateUser.setLastName(UUID.randomUUID().toString());


		Map<String, Object> request1 = new HashMap<String, Object>();
		request1.put("service", "user");
		request1.put("action", "add");
		request1.put("user", newUser);

		Map<String, Object> request2 = new HashMap<String, Object>();
		request2.put("service", "user");
		request2.put("action", "get");
		request2.put("id", "{results:1:id}");

		Map<String, Object> request3 = new HashMap<String, Object>();
		request3.put("service", "user");
		request3.put("action", "update");
		request3.put("id", "{results:1:id}");
		request3.put("user", updateUser);
		
		Map<String, Object> request4 = new HashMap<String, Object>();
		request4.put("service", "user");
		request4.put("action", "delete");
		request4.put("id", "{results:1:id}");

		Map<String, Object> request5 = new HashMap<String, Object>();
		request5.put("service", "user");
		request5.put("action", "get");
		request5.put("id", "{results:1:id}");
		
		Map<String, Object> requestData = new HashMap<String, Object>();
		requestData.put("1", request1);
		requestData.put("2", request2);
		requestData.put("3", request3);
		requestData.put("4", request4);
		requestData.put("5", request5);

		RestClientMultiResponse response = sendMultiRequest(requestData, RestClientMultiResponse.class);

		assertNotNull("No results returned", response.getResult());
		RestClientObjectList results = (RestClientObjectList) response.getResult();
		assertEquals(requestData.size(), results.size());

		// add
		RestClientResponse result1 = (RestClientResponse) results.get(0);
		assertTrue("Result is user", result1.getResult() instanceof RestClientUser);
		RestClientUser addedUser = (RestClientUser) result1.getResult();
		assertNotNull("user id is null", addedUser.getId());
		assertNotNull("User createdAt id is null", addedUser.getCreatedAt());
		assertNotNull("User updatedAt id is null", addedUser.getUpdatedAt());
		assertEquals("Status is active", addedUser.getStatus(), UserStatus.Active);
		assertEquals("First name is the same", addedUser.getFirstName(), newUser.getFirstName());
		assertEquals("Last name is the same", addedUser.getLastName(), newUser.getLastName());
		
		// get
		RestClientResponse result2 = (RestClientResponse) results.get(1);
		assertTrue("Result is user", result2.getResult() instanceof RestClientUser);
		RestClientUser gettedUser = (RestClientUser) result2.getResult();
		assertNotNull("user id is null", gettedUser.getId());
		
		// update
		RestClientResponse result3 = (RestClientResponse) results.get(2);
		assertTrue("Result is user", result3.getResult() instanceof RestClientUser);
		RestClientUser updatedUser = (RestClientUser) result3.getResult();
		assertNotNull("user id is null", updatedUser.getId());
		assertNotNull("User createdAt id is null", updatedUser.getCreatedAt());
		assertNotNull("User updatedAt id is null", updatedUser.getUpdatedAt());
		assertEquals("Status is active", updatedUser.getStatus(), UserStatus.Active);
		assertEquals("First name is the same", updatedUser.getFirstName(), updateUser.getFirstName());
		assertEquals("Last name is the same", updatedUser.getLastName(), updateUser.getLastName());
		
		// delete
		RestClientResponse result4 = (RestClientResponse) results.get(3);
		assertTrue("Fourth response has no result",  result4.getResult() == null);
		
		// invalid get
		RestClientResponse result5 = (RestClientResponse) results.get(4);
		assertTrue("Fourth response has no result",  result5.getError() != null);
		RestClientException error = result5.getError();
		assertEquals("Error code is wrong", "OBJECT_NOT_FOUND", error.getCode());
	}
}
