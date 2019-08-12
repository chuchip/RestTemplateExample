package com.profesorp.restTemplate;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
 
@SpringBootApplication
public class RestTemplateExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestTemplateExampleApplication.class, args);
	}

	@PostConstruct
	public void iniciando()
	{
		
	}
	@Bean
	@Qualifier("restHandleError") 
	public RestTemplate createRestTemplateError(CustomResponseErrorHandler errorHandler) {
		RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));				
		restTemplate.setErrorHandler(errorHandler);			
		return restTemplate;
	}
	
	@Bean
	@Qualifier("restInterceptor")
	public RestTemplate createRestTemplateInterceptor(CustomResponseErrorHandler errorHandler) {
		RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));				
		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();		
		interceptors.add(new LoggingRequestInterceptor());
		restTemplate.setInterceptors(interceptors);
		return restTemplate;
	}
	
	@Bean
	public CustomResponseErrorHandler getErrorHandler() {
		return new CustomResponseErrorHandler();
	}
}
