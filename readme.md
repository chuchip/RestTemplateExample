En este articulo voy a hablar acerca de la la librería **RestTemplate** de **Spring.**

Como siempre tenéis el proyecto sobre el que esta basado este artículo en: [https://github.com/chuchip/RestTemplateExample](https://github.com/chuchip/RestTemplateExample))

Con esta librería  se realizan peticiones HTTP fácilmente, realizándose la serialización y deserialización  de los objetos de una manera transparente. Es una gran alternativa dada por **Spring** sobre otras librerías ya existentes en el mercado, con la ventaja de que se integra perfectamente con **Spring**.

**RestTemplate** esta en el *core* de **Spring** por lo cual no es necesario instalar ninguna dependencia. Lo puedes encontrar en el paquete `org.springframework.web.client.RestTemplate`

Para hacer una petición a un recurso web con **RestTemplate**  simplemente se escribiría este código:

```java
ResponseEntity<Customer> responseEntity= new RestTemplate().getForEntity(URL, Customer.class);
```

Donde `URL`seria la dirección donde queremos realizar la petición (por ejemplo) y `Customer` es el objeto que esperamos que nos vaya a devolver esa petición. La petición seria del tipo GET ya que hemos utilizado la función `getForEntity`.

Como se puede ver la llamada recibe un objeto `ResponseEntity` donde estará embebido el objeto del tipo indicado.

`ResponseEntity` es una clase de **Spring**  que nos permite comprobar el estado de la petición.  De esa clase utilizaremos habitualmente las siguientes funciones:

- getStatusCode() 

  Esta función nos permitirá saber el estado HTTP retornado por la petición. Devuelve un tipo `HttpStatus`

  Así para saber si el servidor devolvió un **OK** podremos poner 

  ```java
  if (responseEntity.getStatusCode()==HttpStatus.OK)
  { // Todo fue bien
  ....
  }
  ```

* getHeaders()

  Devuelve un objeto `HttpHeaders` en el cual tendremos las cabeceras devueltas por el servidor.

* getBody()

  Devuelve una instancia de la clase devuelto por el servidor. En nuestro ejemplo anterior devolvería un objeto del tipo `Customer`

Vamos a poner un ejemplo para aclarar las ideas.

Supongamos que nuestro servidor esperamos que nos devuelva los datos de un cliente cuando hagamos una petición a una URL determinada. La clase devuelta seria esta:

```java
import lombok.Data;

@Data
public class Customer {
	private String name;
	private String address;
}
```

Cuando todo vaya bien nos devolverá un código HTTP 200 (OK)

Para hacer la petición utilizaríamos el siguiente código:

```java
public Customer getCliente()
{
    ResponseEntity<Customer> responseEntity;
    responseEntity=restTemplate.getForEntity("http://localhost:8080", Customer.class);

    if (responseEntity.getStatusCode()==HttpStatus.OK)
        return responseEntity.getBody();
    throw new RuntimeException("El servidor NO devolvio OK");
}
```

Como se ve esta función devolvería el objeto `Customer` si el servidor devuelve un OK o lanzaría una excepción en el caso contrario. 

Fácil, ¿ verdad ?. Pero, ¿ no os parece que falta algo ?. Haceros las siguientes preguntas:

1. ¿Como puedo capturar el mensaje de error devuelto por el servidor si el código devuelto no es OK?
2. ¿Que pasa si la llamada falla porque el servidor esta caído?
3. Y si el servidor devuelve un OK, pero lo devuelto no es un objeto del tipo `Customer`,  ¿ que pasara?.
4. ¿Como podría tener un registro de lo enviado y recibido por el servidor ?
5. ¿Me devolverá el objeto tipo `Customer` en el cuerpo de la respuesta aunque no sea OK el estado de esta?

Bueno, pues no todas son tan obvias como podría parecer.  Voy a responder a ellas.

### 1. ¿Como puedo capturar el mensaje de error devuelto por el servidor si el código devuelto no es OK?

En la aplicación de ejemplo si realizamos esta llamada.

```bash
curl -s http:/localhost:8080
```

El servidor devolverá el código HTTP  **BAD_REQUEST** . Esta sería la respuesta completa.

```
{"timestamp":"2019-08-01T12:47:56.635+0000","status":400,"error":"Bad Request","message":"Give me a customer!","path":"/"}
```

Si **Spring** intentara meter esa variable en el objeto `Customer` no veríamos nada pues nosotros no tenemos definida ni la variable *timestamp* ni *status* ni *error*. Así que, ¿ como conseguir capturar ese mensaje de error para ver que ha pasado ?.

Hay dos opciones:

#### 1.1 Capturar la excepción del tipo HttpClientErrorException



#### 1.2 Establecer un manejador de Errores personalizados

En la función `setErrorHandler` de la clase **RestTemplate**. Si llamamos a esa función con un objeto que implemente el interface  `ResponseErrorHandler` podremos capturar la salida de error. 

Así en el proyecto al crear la clase **RestTemplate** que se utilizara tenemos este código:

```java
@Bean
public RestTemplate createRestTemplate() {
    RestTemplate restTemplate = new RestTemplate();				
    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
    restTemplate.setErrorHandler(getErrorHandler());
    interceptors.add(new LoggingRequestInterceptor(getErrorHandler()));
    restTemplate.setInterceptors(interceptors);		
    return restTemplate;
}
@Bean
public CustomResponseErrorHandler getErrorHandler() {
    return new CustomResponseErrorHandler();
}
```

La clase `CustomResponseErrorHandler` que implementa el interface `ResponseErrorHandler ` es la siguiente:

```java
public class CustomResponseErrorHandler implements ResponseErrorHandler {
	StringBuilder body;
	StringBuilder msgError;
	HttpStatus estado;

	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		if (msgError==null)
			msgError=new StringBuilder();
		if (body==null)
			getBody(response);
		msgError.append("Codigo HTTP: " + response.getStatusCode().toString() + "\n Cuerpo Mensaje:\n "+body.toString());
		
	}		
	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		estado = response.getStatusCode();
		return response.getStatusCode() != HttpStatus.OK;
	}
    public StringBuilder getBody(ClientHttpResponse response) throws IOException
	{		
		byte[] b = new byte[100];
		int br;
		body=new StringBuilder();
		InputStream is = response.getBody();
		while (true) {
			br = is.read(b, 0, 100);
			if (br == -1)
				break;
			body.append(new String(b, 0, br));
		}
		return body;
	}
	public String getBody() {
		if (body==null)
			return null;
		return body.toString();
	}
	
	public HttpStatus getEstado() {
		return estado;
	}
	public void setMsgError(String mensajeError)
	{
		if (msgError==null)
			msgError=new StringBuilder();
	}
	public String getMsgError() {
		return msgError.toString();
	}
	public void reset()
	{
		msgError=null;
		body=null;
		estado=null;
	}
}
```

Este interface nos obliga a tener las funciones `hasError` y `handleError` con la primera definiremos cuando consideramos nosotros que ha habido un error.  En el ejemplo se define que si  el código HTTP es diferente de OK  considere que hay un error.

Si la función `hasError` devuelve true, la función `handleError` será llamada y ahí es donde podremos capturar la respuesta del servidor y tratarla. En el ejemplo simplemente la guardamos en la variable **msgError** .

Ahora, podremos capturar el mensaje devuelto por el servidor con este código: 

```java
if (!httpStatus.is2xxSuccessful())
	mensajeError=customError.getMsgError();
```

Es importante destacar que si el servidor NO devuelve un código HTTP del tipo 2XX el cuerpo de la respuesta será siempre igual a NULL, aunque haya devuelto algo.

#### 1.1 Practica

En el proyecto de ejemplo, cuando realizamos una petición a:

```java
curl -s http:/localhost:8080/ERROR
```

El servidor devuelve una código HTTP 400 (BAD REQUEST) y el siguiente *body* `{"name":"Customer ERROR","address":"Address Customer ERROR"}`

Sin embargo si llamamos a la función **getBody(**) del objeto `ResponseEntity`  veremos como nos devuelve un NULL y deberemos llamar a la función **getBody()** de la clase `CustomResponseErrorHandler` como se puede ver en el código.

```java
ResponseEntity<String> responseEntity=restTemplate.getForEntity(localUrl, String.class);			
HttpStatus httpStatus= responseEntity.getStatusCode();
String mensaje="Http Status: "+httpStatus+" -> ";
if (httpStatus.is2xxSuccessful())
	mensaje+=responseEntity.getBody();
else
	mensaje+=customError.getBody();
```

### 2. ¿Que pasa si la llamada falla porque el servidor esta caído?

En ese caso la llamada a la función `getForEntity` lanzara una excepción del tipo `RestClientException`. En esta clase podremos conseguir información sobre el error generado.



Ejemplo de llamadas

```bash
curl -s http:/localhost:8080?nameCustomer=aa

curl -s http:/localhost:8080?nameCustomer=ERROR

curl -s http:/localhost:8080
```


# Alcance

RestTemplate 

Si el código HTTP  devuelto no es un 2XX el body siempre sera null. 
Para ver el mensaje de error deberemos usar un errorHandler.

Si el codigo HTTP es un 2XX aunque el objeto devuelto no sea el esperado simplemente devolvera el objeto esperado con todos sus campos a null.

Es decir, si se hace  una llamada como la siguiente:

```
ResponseEntity<Customer> responseEntity=restTemplate.getForEntity(localUrl, Customer.class);
```

Y lo que devuelve esa llamada es un objeto que no es del tipo Customer ni compatible con el, la función `cliente.getBody()` devolvera un objeto Customer que no sera null, pero todos los campos de él si estaran a nulo.

```
ResponseEntity<Customer> responseEntity=restTemplate.getForEntity(localUrl, Customer.class);
if (responseEntity.getStatusCode().is2xxSuccessful())
{
	// cliente.getBody() -> NUNCA SERA NULL
	Customer  customer=responseEntity.getBody();
   //customer.getName(); -> SIEMPRE SERA NULL
}
```

 

1. Programacion declarativa.
1.1 LLamar funcion get
1.2 Llamar funcion post
1.3 Llamar funcion exchange
1.4 Llamar funcion exchange con ParametizerTypeReference
1.5 Capturar errores con ResponseErrorHandler
1.6 Poner logs con interceptor

1.6 Casos de error, cuando la clase devuelta no es la esperada.

2. Programacion Funcional. Usando Feign




