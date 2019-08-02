package com.profesorp.restTemplate.impl;

import javax.annotation.PostConstruct;

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
 *
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

    @PostConstruct
    public void iniciar() {
        String puerto = enviroment.getProperty("server.port");
        if (puerto == null) {
            puerto = "8080";
        }
        url = url + puerto;
    }

    public String peticionGetPersonalizada(String idCliente) {
       
        String localUrl = url;
        if (idCliente != null) {
            localUrl += "?nameCustomer=" + idCliente;
        }
        if ("DOWN".equals(idCliente)) {
            localUrl = "http://localhost:1111";
        }
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = restTemplate.getForEntity(localUrl, String.class);
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

    public String peticionGet(String idCliente) {
        String localUrl = url;
        if (idCliente != null) {
            localUrl += "?nameCustomer=" + idCliente;
        }
        if ("DOWN".equals(idCliente)) {
            localUrl = "http://localhost:1111";
        }
        ResponseEntity<String> responseEntity = null;
        try {
            responseEntity = new RestTemplate().getForEntity(localUrl, String.class);
        } catch (HttpClientErrorException k1) {            
            return "Http code is not 2XX. The server responded: " + k1.getStatusCode() + " Cause: "
                    + k1.getResponseBodyAsString();
        } catch (RestClientException k) {
            return "The server didn't respond: " + k.getMessage();
        }
        HttpStatus httpStatus = responseEntity.getStatusCode();
        String mensaje = "Http Status: " + httpStatus + " -> " + responseEntity.getBody();
        return mensaje;
    }

}
