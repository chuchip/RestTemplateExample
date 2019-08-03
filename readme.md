Este será el primero de una serie de entradas sobre la clase  **RestTemplate** de **Spring.**

Con los diferentes métodos de esta clase se realizan peticiones HTTP fácilmente, produciéndose la serialización y deserialización  de los objetos de una manera transparente. Es una gran alternativa dada por **Spring** sobre otras librerías ya existentes en el mercado.

Como siempre tenéis el proyecto sobre el que esta basado este artículo en: [https://github.com/chuchip/RestTemplateExample](https://github.com/chuchip/RestTemplateExample))

**RestTemplate** esta en el *core* de **Spring** por lo cual no es necesario instalar ninguna dependencia. Lo puedes encontrar en el paquete Este será el primero de una serie de entradas sobre la clase  **RestTemplate** de **Spring.**

Con los diferentes métodos de la clase se realizan peticiones HTTP fácilmente, produciéndose la serialización y deserialización  de los objetos de una manera transparente. Es una gran alternativa dada por **Spring** sobre otras librerías ya existentes en el mercado.

Como siempre tenéis el proyecto sobre el que esta basado este artículo en: [https://github.com/chuchip/RestTemplateExample](https://github.com/chuchip/RestTemplateExample)

**RestTemplate** esta en el *core* de **Spring** por lo cual no es necesario instalar ninguna dependencia. Lo puedes encontrar en el paquete `org.springframework.web.client.RestTemplate`

Para hacer una petición a un recurso web con **RestTemplate**  simplemente se escribiría este código:

```
ResponseEntity<Customer> responseEntity= new RestTemplate().getForEntity(URL, Customer.class);
```

Donde `URL`seria la dirección donde queremos realizar la petición (por ejemplo) y `Customer` es el objeto que esperamos que nos vaya a devolver esa petición. La petición seria del tipo GET ya que hemos utilizado la función `getForEntity`.

En esa llamada se recibe un objeto `ResponseEntity` donde estará embebido el objeto del tipo indicado, pero si solo nos interesa recoger el cuerpo del mensaje podríamos utilizar  la función `getForObject`, sin embargo el recoger la clase `ResponseEntity` nos ofrece una información que a menudo es necesaria.

De esta clase podremos utilizar, entre otras, las siguientes funciones:

- **getStatusCode()** 

  Esta función nos permitirá saber el estado HTTP retornado por la petición. Devuelve un tipo `HttpStatus`

  Así para saber si el servidor devolvió un **OK** podremos poner 

  ```
  if (responseEntity.getStatusCode()==HttpStatus.OK)
  { // Todo fue bien
  ....
  }
  ```

- **getHeaders()**

  Devuelve un objeto `HttpHeaders` en el cual tendremos las cabeceras devueltas por el servidor.

- **getBody()**

  Devuelve una instancia de la clase devuelto por el servidor. En nuestro ejemplo anterior devolvería un objeto del tipo `Customer`

Supongamos que nuestro servidor esperamos que nos devuelva los datos de un cliente. La clase devuelta seria esta:

```
public class Customer {
	private String name;
	private String address;
}
```

Si todo va bien nos devolverá un código HTTP 200 (OK)

Para hacer la petición utilizaremos el siguiente código:

```
public Customer getCliente()
{
    ResponseEntity<Customer> responseEntity =  restTemplate.getForEntity("http://localhost:8080", Customer.class);

    if (responseEntity.getStatusCode()==HttpStatus.OK)
        return responseEntity.getBody();
    throw new RuntimeException("The server didn't respond OK");
}
```

Como se ve esta función devolvería el objeto `Customer` si el servidor devuelve un OK o lanzaría una excepción en el caso contrario. 

Fácil, ¿ verdad ?. Pero, ¿ no os parece que falta algo ?. Haceros las siguientes preguntas:

1. ¿Como puedo capturar el mensaje de error devuelto por el servidor si el código devuelto no es OK?
2. ¿Que pasa si la llamada falla porque el servidor esta caído?
3. Y si el servidor devuelve un OK, pero lo devuelto no es un objeto del tipo `Customer`,  ¿ que pasara?.
4. ¿Como podría tener un registro de lo enviado y recibido por el servidor ?
5. ¿Me devolverá el objeto tipo `Customer` en el cuerpo de la respuesta aunque no sea OK el estado de esta?

Bueno, pues no todas son tan obvias como podría parecer.  Voy a intentar responder a ellas.

## 1. ¿Como puedo capturar el mensaje de error devuelto por el servidor si el código devuelto no es OK?

En la aplicación de ejemplo si realizamos esta llamada.

```
curl -s http:/localhost:8080/NULL
```

El servidor lanzara una excepción con el el código HTTP  **BAD_REQUEST** y obtendremos la siguiente salida:

```
{"timestamp":"2019-08-03T04:45:54.458+0000","status":400,"error":"Bad Request","message":"....""path":"/"}
```

Si **Spring** intentara meter esa variable en el objeto `Customer` no veríamos nada pues nosotros no tenemos definida ni la variable *timestamp* ni *status* ni *error*. Así que, ¿ como conseguir capturar ese mensaje de error para ver que ha pasado ?.

Hay dos opciones:

### 1.1 Capturar la excepción del tipo HttpClientErrorException

Este sería el método fácil. Para ello simplemente deberemos meter entre un **try/catch** la llamada a la función de RestTemplate.

```
try {
    responseEntity = new RestTemplate().getForEntity(localUrl, String.class);
} catch (HttpClientErrorException k1) {            
    return "Http code is not 2XX. The server responded: " + k1.getStatusCode() + 
        " Cause: "+ k1.getResponseBodyAsString();
} catch (RestClientException k) {
    return "The server didn't respond: " + k.getMessage();
}
```

Ahora si la respuesta no es del tipo 2XX la llamada lanzara una excepción tipo `HttpClientErrorException` y a través de ella podremos capturar el mensaje y el  código HTTP, devuelto, así como las correspondientes cabeceras.

#### 1.1.1 Practica

Si realizamos una llamada como esta: 

```
 curl -s http:/localhost:8080/ERROR
```

 El servidor llamado por el programa devolvera una código HTTP 400 (BAD REQUEST) y el siguiente *body* `{"name":"Customer ERROR","address":"Address Customer ERROR"}` . 

Al ser un código HTTP que no esta en el rango de 200-300 (2XX) se lanzara la excepción `HttpClientErrorException`  y obtendríamos la siguiente respuesta:

```
Error al realizar petición HTTP. Codigo retornado: 400 BAD_REQUEST Causa: {"name":"Customer ERROR","address":"Address Customer ERROR"}
```

Si realizamos la llamada:

```
curl -s http:/localhost:8080/DOWN
```

Se intentara realizar una petición al puerto 1111, donde no habrá nada escuchando y por lo tanto se lanzara la excepción `RestClientException`, obteniendo esta respuesta:

```
Servidor no respondio: I/O error on GET request for "http://localhost:1111": Connection refused: connect; nested exception is java.net.ConnectException: Connection refused: connect
```

### 1.2 Establecer un manejador de Errores personalizados

Con este método tendremos un manera para decidir nosotros que consideramos un error y realizar acciones sobre él. Además en el caso de no utilizar llamadas que devuelvan un objeto `ResponseEntity` sino el objeto en si podremos acceder a la cabeceras, cuerpo de mensaje, y código devuelto de una manera más personalizada.

Si llamamos a  la función `setErrorHandler` de la clase **RestTemplate** con  a  objeto  que implemente el interface  `ResponseErrorHandler` podremos capturar la salida de error. 

Así en el proyecto al crear la clase **RestTemplate** que se utilizara tenemos este código:

```
@Bean
public RestTemplate createRestTemplate() {
    RestTemplate restTemplate = new RestTemplate();				
    restTemplate.setErrorHandler(getErrorHandler());
    return restTemplate;
}
@Bean
public CustomResponseErrorHandler getErrorHandler() {
    return new CustomResponseErrorHandler();
}
```

La clase `CustomResponseErrorHandler` que implementa el interface `ResponseErrorHandler ` es la siguiente:

```
public class CustomResponseErrorHandler implements ResponseErrorHandler {
	StringBuilder body;	
    
	@Override
	public void handleError(ClientHttpResponse response) throws IOException {		
		if (body==null)
			getBody(response);		
	}
	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {		
		return response.getStatusCode() != HttpStatus.OK;
	}
	public StringBuilder getBody(ClientHttpResponse response) throws IOException
	{				
		log.trace("Leido body de respuesta");
		body=new StringBuilder();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), "UTF-8"));
		String line = bufferedReader.readLine();
		while (line != null) {
			body.append(line);
			body.append('\n');
			line = bufferedReader.readLine();
		}	
		return body;
	}
	public String getBody() {
		if (body==null)
			return null;
		return body.toString();
	}	
	public void reset()
	{	
		body=null;
	}
}
```

Este interface nos obliga a tener las funciones `hasError` y `handleError` con la primera definiremos cuando consideramos nosotros que ha habido un error.  En el ejemplo se define que si  el código HTTP es diferente de OK  considere que hay un error.

Si la función `hasError` devuelve true, la función `handleError` será llamada y ahí es donde podremos capturar la respuesta del servidor y tratarla. En el ejemplo simplemente la guardamos en la variable **msgError** .

Ahora, podremos capturar el mensaje devuelto por el servidor con este código: 

```
if (!httpStatus.is2xxSuccessful())
	mensajeError=customError.getMsgError();
```

Es importante destacar que si el servidor NO devuelve un código HTTP del tipo 2XX el cuerpo de la respuesta del será siempre igual a NULL, `ResponseEntity`  aunque el servidor haya devuelto algo.

#### 1.2.1 Practica

En el proyecto de ejemplo, cuando realizamos una petición a:

```
curl -s http:/localhost:8080/custom/ERROR
```

Se ejecutara el código de la función `peticionGetPersonalizada`

```
 public String peticionGetPersonalizada(String idCliente) {    
     String localUrl = url;
     if (idCliente != null) {
         localUrl += "?path=" + idCliente;
     }
     if ("DOWN".equals(idCliente)) {
         localUrl = "http://localhost:1111";
     }
     ResponseEntity<String> responseEntity = null;
     try {
         responseEntity = restTemplate.getForEntity(localUrl, String.class);
     } catch (RestClientException k) {
         return "Custom RestTemplate. The server didn't respond: " + k.getMessage();
     }
     HttpStatus httpStatus = responseEntity.getStatusCode();
     String mensaje = "Http Status: " + httpStatus + " -> ";
     if (httpStatus.is2xxSuccessful()) {
         mensaje += "Body: " + responseEntity.getBody();
     } else {
         mensaje += " Error message: "+ customError.getBody();
     }
     return mensaje;
 }
```

Por lo tanto se obtendrá esta salida, ya que el código HTTP es 400 y se por lo tanto se el cuerpo de la respuesta de la clase **customError**

```
Http Status: 400 BAD_REQUEST ->  Error message: {"name":"Customer ERROR","address":"Address Customer ERROR"}
```

En nuestro ejemplo si pasamos el parámetro CREATED, el servidor devolverá un objeto `Customer` con el código HTTP **CREATED** . Como en la clase `CustomResponseErrorHandler` se ha definido que cualquier código HTTP diferente de **OK** será considerada un error,  el cuerpo del mensaje será NULL.

```
> curl -s http:/localhost:8080/custom/CREATED
Http Status: 201 CREATED -> Body: null

```



## 2.  ¿Que pasa si la llamada falla porque el servidor esta caído?

En ese caso, tengamos o no tengamos establecido un **handleError**  la llamada a la función `getForEntity` lanzara una excepción del tipo `RestClientException`. En esta clase podremos conseguir información sobre el error generado.

```
curl -s http:/localhost:8080/DOWN
The server didn't respond: I/O error on GET request for "http://localhost:1111": Connection refused: connect; nested exception is java.net.ConnectException: Connection refused: connect

```


### 3. Y si el servidor devuelve un OK, pero lo devuelto no es un objeto del tipo `Customer`,  ¿ que pasara?.




# Alcance

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

2. Programacion Funcional. Usando Feign




