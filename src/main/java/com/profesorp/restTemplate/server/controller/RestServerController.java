package com.profesorp.restTemplate.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.profesorp.restTemplate.MyAcceptedException;
import com.profesorp.restTemplate.MyException;
import com.profesorp.restTemplate.impl.RestClient;
import com.profesorp.restTemplate.server.dto.Customer;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class RestServerController {
	@Autowired
	RestClient cliente;

	@GetMapping
	public ResponseEntity<Customer> getCustomer(@RequestParam(required = false) String queryParam) {
		log.debug("Received request at getCustomer:" + queryParam);
		if (queryParam == null  || "NULL".equals(queryParam))
			throw new MyException("Give me a customer!");

		Customer customer = new Customer();
		customer.setName("Customer " + queryParam);
		customer.setAddress("Address Customer " + queryParam);
		if (queryParam.equals("ERROR"))
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(customer);
		if (queryParam.equals("CREATED"))
			return ResponseEntity.status(HttpStatus.CREATED).body(customer);
		if (queryParam.equals("ACCEPT"))
			throw new MyAcceptedException("Don't send me accepts!!");
		return ResponseEntity.ok().body(customer);
	}

	@PostMapping
	public ResponseEntity<Customer> postCustomer(@RequestBody Customer customer) {
		if (customer == null)
			throw new RuntimeException("Give me a customer!");
		if (customer.getName().equals("exception"))
			throw new RuntimeException("I hate exceptions!");
		return ResponseEntity.ok().body(customer);
	}

	
}
