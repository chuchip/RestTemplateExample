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
	String ERROR = "ERROR";
	@Autowired
	RestClient cliente;

	@GetMapping
	public ResponseEntity<Customer> getCustomer(@RequestParam(required = false) String nameCustomer) {
		log.debug("Recibida petición en getCustomer:" + nameCustomer);
		if (nameCustomer == null)
			throw new MyException("Give me a customer!");

		Customer customer = new Customer();
		customer.setName("Customer " + nameCustomer);
		customer.setAddress("Address Customer " + nameCustomer);
		if (nameCustomer.equals(ERROR))
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(customer);
		if (nameCustomer.equals("ACCEPT"))
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

	@GetMapping("/{tipoTest}")
	public ResponseEntity<String> testGet(@PathVariable String tipoTest) {
		log.debug("Recibida petición tipo:" + tipoTest);
		String response = "";
		switch (tipoTest.toUpperCase()) {
		case "NULL":
			response = cliente.peticionGet(null);
			break;
		default:
			response = cliente.peticionGet(tipoTest);
		}
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/custom/{tipoTest}")
	public ResponseEntity<String> testGetPersonalizado(@PathVariable String tipoTest) {
		log.debug("Recibida petición personalizada tipo:" + tipoTest);
		String response = "";
		switch (tipoTest.toUpperCase()) {
		case "NULL":
			response = cliente.peticionGetPersonalizada(null);
			break;
		default:
			response = cliente.peticionGetPersonalizada(tipoTest);
		}
		return ResponseEntity.ok().body(response);
	}
}
