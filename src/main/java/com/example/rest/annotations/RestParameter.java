package com.example.rest.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RestParameter {
	public static String NULL = "NULL";
	
	String name();
	boolean required() default true;
	String defaultValue() default NULL;
	String description() default "";
}
