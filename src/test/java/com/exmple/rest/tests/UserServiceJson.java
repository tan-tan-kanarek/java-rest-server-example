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
import com.example.rest.json.RestClientException;
import com.example.rest.json.RestClientMultiResponse;
import com.example.rest.json.RestClientPager;
import com.example.rest.json.RestClientResponse;
import com.example.rest.json.RestClientUser;
import com.example.rest.json.RestClientUserFilter;
import com.example.rest.json.RestClientUserListResponse;
import com.example.rest.json.RestClientUserResponse;
import com.example.rest.json.RestClientUsersList;

public class UserServiceJson {

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
		Invocation.Builder invocationBuilder = target.request(MediaType.APPLICATION_JSON_TYPE);
		T response = invocationBuilder.post(Entity.json(requestData), type);
		return response;
	}
	
	private <T> T sendMultiRequest(Map<String, Object> requestData, Class<T> type)
	{
		return sendRequest("multirequest", null, requestData, type);
	}
	
	private RestClientUser add() {
		
		RestClientUser newUser = new RestClientUser();
		newUser.setFirstName("first" + UUID.randomUUID().toString());
		newUser.setLastName("last" + UUID.randomUUID().toString());
		
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

		assertEquals(requestData.size(), response.getResult().size());

		// add
		@SuppressWarnings("unchecked")
		Map<String, Object> result1 = (Map<String, Object>) response.getResult().get(0);
		assertTrue("First response has result",  result1.containsKey("result"));
		@SuppressWarnings("unchecked")
		Map<String, Object> addedUser = (Map<String, Object>) result1.get("result");
		assertTrue("User id is null",  addedUser.containsKey("id"));
		assertTrue("User id is numeric",  addedUser.get("id") instanceof Integer);
		assertTrue("User createdAt is null",  addedUser.containsKey("createdAt"));
		assertTrue("User createdAt is numeric",  addedUser.get("createdAt") instanceof Integer);
		assertTrue("User updatedAt is null",  addedUser.containsKey("updatedAt"));
		assertTrue("User updatedAt is numeric",  addedUser.get("updatedAt") instanceof Integer);
		assertEquals("First name is the same", addedUser.get("firstName"), newUser.getFirstName());
		assertEquals("Last name is the same", addedUser.get("lastName"), newUser.getLastName());
		
		// get
		@SuppressWarnings("unchecked")
		Map<String, Object> result2 = (Map<String, Object>) response.getResult().get(1);
		assertTrue("Second response has result",  result2.containsKey("result"));
		@SuppressWarnings("unchecked")
		Map<String, Object> gettedUser = (Map<String, Object>) result2.get("result");
		assertTrue("User id is null",  gettedUser.containsKey("id"));
		assertEquals(addedUser.get("id"), gettedUser.get("id"));
		
		// update
		@SuppressWarnings("unchecked")
		Map<String, Object> result3 = (Map<String, Object>) response.getResult().get(2);
		assertTrue("Third response has result",  result3.containsKey("result"));
		@SuppressWarnings("unchecked")
		Map<String, Object> updatedUser = (Map<String, Object>) result3.get("result");
		assertTrue("User id is null",  updatedUser.containsKey("id"));
		assertTrue("User id is numeric",  updatedUser.get("id") instanceof Integer);
		assertTrue("User createdAt is null",  updatedUser.containsKey("createdAt"));
		assertTrue("User createdAt is numeric",  updatedUser.get("createdAt") instanceof Integer);
		assertTrue("User updatedAt is null",  updatedUser.containsKey("updatedAt"));
		assertTrue("User updatedAt is numeric",  updatedUser.get("updatedAt") instanceof Integer);
		assertEquals("First name is the same", updatedUser.get("firstName"), updateUser.getFirstName());
		assertEquals("Last name is the same", updatedUser.get("lastName"), updateUser.getLastName());
		
		// delete
		@SuppressWarnings("unchecked")
		Map<String, Object> result4 = (Map<String, Object>) response.getResult().get(3);
		assertTrue("Fourth response has no result",  !result4.containsKey("result") || result4.get("result") == null);
		
		// invalid get
		@SuppressWarnings("unchecked")
		Map<String, Object> result5 = (Map<String, Object>) response.getResult().get(4);
		assertTrue("Fifth response has no result",  result5.containsKey("error"));
		@SuppressWarnings("unchecked")
		Map<String, Object> error = (Map<String, Object>) result5.get("error");
		assertTrue("Error code is null",  error.containsKey("code"));
		assertEquals("Error code is wrong", "OBJECT_NOT_FOUND", error.get("code"));
	}
}
