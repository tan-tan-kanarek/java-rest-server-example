package com.example.rest.db;

import java.util.ArrayList;
import java.util.List;

public class RestCriterion {

	public static final RestCriterionType AND = new RestCriterionType("AND");
	public static final RestCriterionType OR = new RestCriterionType("OR");
	
	public static final String GREATER_THAN_OR_EQUAL = ">=";
	public static final String LESS_THAN_OR_EQUAL = "<=";

	private RestCriterionType type;
	
	private List<RestCriterion> criterions = null;
	
	private String condition = null;

	public static class RestCriterionType
	{
		private String operator;

		public RestCriterionType(String operator) {
			this.operator = operator;
		}
	}
	
	public RestCriterion(RestCriterionType operatorType) {
		type = operatorType;
	}

	public RestCriterion(String column, Long value, String operator) {
		condition = column + " " + operator + " " + value;
	}

	public void add(RestCriterion restCriterion) {
		if(criterions == null)
			criterions = new ArrayList<RestCriterion>();
		
		criterions.add(restCriterion);
	}

	public String toString()
	{
		String criterion = condition;
		
		if(criterions != null)
		{
			List<String> conditions = new ArrayList<String>();

			if(condition != null)
				conditions.add(condition);
			
			for(RestCriterion subCriterion : criterions)
				conditions.add(subCriterion.toString());
			
			criterion = "(" + String.join(type.operator, conditions.toArray(new String[conditions.size()])) + ")";
		}
		
		return criterion;
	}
}
