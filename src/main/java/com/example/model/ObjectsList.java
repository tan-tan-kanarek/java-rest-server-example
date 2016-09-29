package com.example.model;

import com.example.rest.RestObject;

public abstract class ObjectsList extends RestObject {
	private int TotalCount;

	public int getTotalCount() {
		return TotalCount;
	}

	public void setTotalCount(int totalCount) {
		TotalCount = totalCount;
	}
}
