package com.example.rest.xml;

public class RestClientUserFilter {

	private Long createdAtGreaterThanOrEqual;
	
	private Long createdAtLessThanOrEqual;
	
	private Long updatedAtGreaterThanOrEqual;
	
	private Long updatedAtLessThanOrEqual;

	public Long getCreatedAtGreaterThanOrEqual() {
		return createdAtGreaterThanOrEqual;
	}

	public void setCreatedAtGreaterThanOrEqual(Long createdAtGreaterThanOrEqual) {
		this.createdAtGreaterThanOrEqual = createdAtGreaterThanOrEqual;
	}

	public Long getCreatedAtLessThanOrEqual() {
		return createdAtLessThanOrEqual;
	}

	public void setCreatedAtLessThanOrEqual(Long createdAtLessThanOrEqual) {
		this.createdAtLessThanOrEqual = createdAtLessThanOrEqual;
	}

	public Long getUpdatedAtGreaterThanOrEqual() {
		return updatedAtGreaterThanOrEqual;
	}

	public void setUpdatedAtGreaterThanOrEqual(Long updatedAtGreaterThanOrEqual) {
		this.updatedAtGreaterThanOrEqual = updatedAtGreaterThanOrEqual;
	}

	public Long getUpdatedAtLessThanOrEqual() {
		return updatedAtLessThanOrEqual;
	}

	public void setUpdatedAtLessThanOrEqual(Long updatedAtLessThanOrEqual) {
		this.updatedAtLessThanOrEqual = updatedAtLessThanOrEqual;
	}
}
