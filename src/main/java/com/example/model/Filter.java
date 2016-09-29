package com.example.model;

import com.example.rest.db.RestCriterion;
import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class Filter {
	@JsonIgnore
	abstract public RestCriterion getCriterion();
}
