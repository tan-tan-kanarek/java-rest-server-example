package com.example.model;

import com.example.rest.db.RestCriterion;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserFilter extends Filter {

	private Long createdAtGreaterThanOrEqual;
	
	private Long createdAtLessThanOrEqual;
	
	private Long updatedAtGreaterThanOrEqual;
	
	private Long updatedAtLessThanOrEqual;

	@JsonIgnore
	public RestCriterion getCriterion() {
		if(createdAtGreaterThanOrEqual == null && createdAtLessThanOrEqual == null && updatedAtGreaterThanOrEqual == null && updatedAtLessThanOrEqual == null)
			return null;
			
		RestCriterion criterion = new RestCriterion(RestCriterion.AND);

		if(createdAtGreaterThanOrEqual != null)
			criterion.add(new RestCriterion("created_at", createdAtGreaterThanOrEqual, RestCriterion.GREATER_THAN_OR_EQUAL));

		if(createdAtLessThanOrEqual != null)
			criterion.add(new RestCriterion("created_at", createdAtLessThanOrEqual, RestCriterion.LESS_THAN_OR_EQUAL));

		if(updatedAtGreaterThanOrEqual != null)
			criterion.add(new RestCriterion("updated_at", updatedAtGreaterThanOrEqual, RestCriterion.GREATER_THAN_OR_EQUAL));

		if(updatedAtLessThanOrEqual != null)
			criterion.add(new RestCriterion("updated_at", updatedAtLessThanOrEqual, RestCriterion.LESS_THAN_OR_EQUAL));
		
		return criterion;
	}

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
