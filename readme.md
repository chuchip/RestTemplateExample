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




