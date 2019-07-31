package com.profesorp.restTemplate;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MyException extends RuntimeException{

	private static final long serialVersionUID = 7626621872124381457L;
	public MyException(String msg)
	{
		super(msg);
	}
}
