package com.profesorp.restTemplate;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class RestTemplateExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestTemplateExampleApplication.class, args);
	}

	@Bean
	public RestTemplate createRestTemplate(CustomResponseErrorHandler errorHandler) {
		RestTemplate restTemplate = new RestTemplate();				
		restTemplate.setErrorHandler(errorHandler);
//		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();		
//		interceptors.add(new LoggingRequestInterceptor(errorHandler));
//		restTemplate.setInterceptors(interceptors);
		return restTemplate;
	}

	@Bean
	public CustomResponseErrorHandler getErrorHandler() {
		return new CustomResponseErrorHandler();
	}
}
