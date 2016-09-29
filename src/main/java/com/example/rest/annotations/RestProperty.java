package com.example.rest.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RestProperty {
	String name();
	String column() default "";
	String description() default "";
	boolean insertOnly() default false;
	boolean readOnly() default false;
	boolean writeOnly() default false;
	int minLength() default Integer.MIN_VALUE;
	int maxLength() default Integer.MAX_VALUE;
	double minValue() default Double.MIN_VALUE;
	double maxValue() default Double.MAX_VALUE;
}
