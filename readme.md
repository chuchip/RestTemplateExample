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




