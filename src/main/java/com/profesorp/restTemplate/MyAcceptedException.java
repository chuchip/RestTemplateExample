package com.profesorp.restTemplate;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.ACCEPTED)
public class MyAcceptedException extends RuntimeException{

	private static final long serialVersionUID = 7626621872124381457L;
	public MyAcceptedException(String msg)
	{
		super(msg);
	}
}
