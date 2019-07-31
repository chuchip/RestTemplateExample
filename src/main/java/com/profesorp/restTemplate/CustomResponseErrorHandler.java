package com.profesorp.restTemplate;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class CustomResponseErrorHandler implements ResponseErrorHandler {

	StringBuffer msgError = new StringBuffer();
	HttpStatus estado;

	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		byte[] b = new byte[100];
		int br;
		msgError.append("Codigo HTTP: " + response.getStatusCode().toString() + "\n Cuerpo Mensaje: \n");
		InputStream is = response.getBody();
		while (true) {
			br = is.read(b, 0, 100);
			if (br == -1)
				break;
			msgError.append(new String(b, 0, br));
		}
	}

	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		estado = response.getStatusCode();
		return response.getStatusCode() != HttpStatus.OK;
	}

	public HttpStatus getEstado() {
		return estado;
	}

	public String getMsgError() {
		return msgError.toString();
	}
	public void reset()
	{
		msgError=new StringBuffer();
		estado=null;
	}
}
