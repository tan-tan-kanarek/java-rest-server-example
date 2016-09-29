package com.example.rest.response;

import java.io.Writer;

import javax.servlet.ServletResponse;

public interface IRestResponseSerializer {

	void serialize(ServletResponse response, Writer writer) throws Exception;
}