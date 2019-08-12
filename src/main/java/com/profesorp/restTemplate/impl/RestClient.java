package com.profesorp.restTemplate.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.profesorp.restTemplate.CustomResponseErrorHandler;
import com.profesorp.restTemplate.server.dto.Customer;

/**
 * Llamada a RestTemplate sin personalizar.
 *
 * @author usuario
 *
 */
@Component
public class RestClient {
    @Autowired
    @Qualifier("restHandleError") 
    RestTemplate restTemplateHandleError;

    @Autowired
    @Qualifier("restInterceptor") 
    RestTemplate restTemplateInterceptor;
    
    @Autowired
    CustomResponseErrorHandler customError;

    @Autowired
    Environment enviroment;

    String url = "http://localhost:";

    @PostConstruct
    public void iniciar() {
        String puerto = enviroment.getProperty("server.port");
        if (puerto == null) {
            puerto = "8080";
        }
        url = url + puerto;        
    }
    /**
     * Envia una peticion a REST con customErrorHandler para simular la llamada a un servidor externo
     * @param path
     * @return
     */
    public String peticionGetPersonalizada(String path) {
       
        String localUrl = url;
        if (path != null) {
            localUrl += "?queryParam=" + path;
        }
        if ("DOWN".equals(path)) {
            localUrl = "http://localhost:1111";
        }
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplateHandleError.getForEntity(localUrl, String.class);
        } catch (RestClientException k) {
            return  "Custom RestTemplate. The server didn't respond: " + k.getMessage();
        }
        HttpStatus httpStatus = responseEntity.getStatusCode();
        String mensaje = "Http Status: " + httpStatus + " -> ";
        if (httpStatus.is2xxSuccessful()) {
            mensaje += "Body: " + responseEntity.getBody();
        } else {
            mensaje += " Message error: "+ customError.getBody();
        }
        return mensaje;
    }
    /**
     * Envia una peticion a REST estandard para simular la llamada a un servidor externo
     * @param path
     * @return
     */
    public String peticionGet(String path) {
        String localUrl = url;
        if (path != null) {
            localUrl += "?queryParam={queryParam}";
        }
        if ("DOWN".equals(path)) {
            localUrl = "http://localhost:1111";
        }
        Map<String,String> sustitute=new HashMap<>();
        sustitute.put("queryParam",path);
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplateInterceptor.getForEntity(localUrl, String.class,sustitute);
        } catch (HttpClientErrorException k1) {            
            return "Http code is not 2XX.\n The server responded: " + k1.getStatusCode() + "\n Cause:\n "
                    + k1.getResponseBodyAsString();
        } catch (RestClientException k) {
            return "The server didn't respond: " + k.getMessage();
        }
        HttpStatus httpStatus = responseEntity.getStatusCode();
        String mensaje = "Http Status: " + httpStatus + " -> " + responseEntity.getBody();
        return mensaje;
    }
    public String peticionPost(Customer customer)
    {
    	   String localUrl = url;
    	   HttpHeaders headers = new HttpHeaders();
    	   headers.setContentType(MediaType.APPLICATION_JSON);
    	   headers.set("my-id", "profe-test");
           HttpEntity<Customer> httpEntity = new HttpEntity<>(customer,headers);
           ResponseEntity<String> responseEntity = null;
           try {
               responseEntity = restTemplateInterceptor.postForEntity( localUrl, httpEntity,String.class);
           } catch (HttpClientErrorException k1) {            
               return "Http code is not 2XX.\n The server responded: " + k1.getStatusCode() + "\n Cause:\n "
                       + k1.getResponseBodyAsString();
           } catch (RestClientException k) {
               return "The server didn't respond: " + k.getMessage();
           }
           HttpStatus httpStatus = responseEntity.getStatusCode();
           String mensaje = "Http Status: " + httpStatus + " -> " + responseEntity.getBody();
           return mensaje;
    }
}
