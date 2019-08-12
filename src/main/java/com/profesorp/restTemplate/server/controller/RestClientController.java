package com.profesorp.restTemplate.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.profesorp.restTemplate.impl.RestClient;
import com.profesorp.restTemplate.server.dto.Customer;

import lombok.extern.slf4j.Slf4j;

@RestController()
@RequestMapping("/client/")
@Slf4j
public class RestClientController {
	@Autowired
	RestClient cliente;

	@GetMapping("{param}")
	public ResponseEntity<String> testGet(@PathVariable String param) {
		log.debug("Client - Received request type GET. Param:" + param);
		String response =  cliente.peticionGet(param);	
		return ResponseEntity.ok().body(response);
	}
	
	@GetMapping("custom/{param}")
	public ResponseEntity<String> testGetPersonalizado(@PathVariable String param) {
		log.debug("Client - Received custom request GET. Param:" + param);
		String response =  cliente.peticionGetPersonalizada(param);
		return ResponseEntity.ok().body(response);
	}
	
	@PostMapping("")
	public ResponseEntity<String> testPost(@RequestBody Customer customer) {
		log.debug("Client - Received custom request type POST. Customer: " + customer);
		String response =  cliente.peticionPost(customer);	
		return ResponseEntity.ok().body(response);
	}
}
