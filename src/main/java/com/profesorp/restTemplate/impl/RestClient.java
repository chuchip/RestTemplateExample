package com.profesorp.restTemplate.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.profesorp.restTemplate.CustomResponseErrorHandler;

/**
 * Llamada a RestTemplate sin personalizar.
 * @author usuario
 *
 */
@Component
public class RestClient {

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	CustomResponseErrorHandler customError;

	@Autowired
	Environment enviroment;

	String url = "http://localhost:";

	public RestClient() {
		String puerto = enviroment.getProperty("server.port");
		if (puerto == null)
			puerto = "8080";
		url = url + puerto;
	}

	public String peticionGetPersonalizada(String idCliente) {
		String localUrl = url;
		if (idCliente != null)
			localUrl += "?nameCustomer=" + idCliente;
		ResponseEntity<String> responseEntity = null;
		try {
			responseEntity = restTemplate.getForEntity(localUrl, String.class);

		} catch (RestClientException k) {
			String msg = "Error tipo restClientException k1" + k.getMessage();
			return msg;
		}
		HttpStatus httpStatus = responseEntity.getStatusCode();
		String mensaje = "Http Status: " + httpStatus + " -> ";
		if (httpStatus.is2xxSuccessful())
			mensaje += "Body: " + responseEntity.getBody();
		else
			mensaje += customError.getBody();
		return mensaje;
	}

	public String peticionGet(String idCliente) {
		String localUrl = url;
		if (idCliente != null)
			localUrl += "?nameCustomer=" + idCliente;
		ResponseEntity<String> responseEntity = null;
		try {
			responseEntity = new RestTemplate().getForEntity(localUrl, String.class);

		} catch (HttpClientErrorException k1) {
			String msg = "Error al realizar petición HTTP. Codigo retornado: " + k1.getStatusCode() + " Causa: "
					+ k1.getResponseBodyAsString();
			return msg;
		} catch (RestClientException k) {
			String msg = "Error tipo restClientException k1" + k.getMessage();
			return msg;
		}
		HttpStatus httpStatus = responseEntity.getStatusCode();
		String mensaje = "Http Status: " + httpStatus + " -> "+	responseEntity.getBody();	
		 
		return mensaje;
	}

	public String peticionServerDown() {
		String localUrl = "http://localhost:1111";
		try {
			new RestTemplate().getForEntity(localUrl, String.class);
		} catch (RestClientException k) {
			return "Error al realizar petición HTTP ->" + k.getMessage();
		}
		return null;
	}
}
