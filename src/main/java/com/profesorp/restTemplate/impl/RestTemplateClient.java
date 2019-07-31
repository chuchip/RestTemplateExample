package com.profesorp.restTemplate.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.profesorp.restTemplate.CustomResponseErrorHandler;
import com.profesorp.restTemplate.MyException;
import com.profesorp.restTemplate.server.dto.Customer;

@Component
public class RestTemplateClient {
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	CustomResponseErrorHandler customError;
	
	@Autowired
	Environment enviroment;
	
	String url="http://localhost:";
	
	public String peticionGet(String idCliente)
	{
		String puerto=enviroment.getProperty("server.port");
		if (puerto==null)
			puerto="8080";
		String localUrl=url+puerto;
		if (idCliente!=null)
			localUrl+="?idCustomer="+idCliente;
		customError.reset();
		ResponseEntity<Customer> responseEntity=restTemplate.getForEntity(localUrl, Customer.class);
		
		ObjectMapper maper=new ObjectMapper();
		String mensaje="";
		try {
			if (responseEntity.getStatusCode().is2xxSuccessful())
				mensaje=maper.writeValueAsString(responseEntity.getBody());
			else
				mensaje=customError.getMsgError();

		} catch (JsonProcessingException e) {
			throw new MyException("I couldn't transform the response object to String!");			
		}
		return mensaje;
	}
	
}
