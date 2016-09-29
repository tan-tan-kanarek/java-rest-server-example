package com.example.rest.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RestAction {
	String name() default "";
	String description() default "";
	boolean enableInMultiRequest() default true;
	String[] thrown() default {};
}
