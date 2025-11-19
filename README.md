# MSCart

Microservicio de gestión de carritos de compra para el ecosistema Arka E-commerce. Utiliza una arquitectura en tres capas (controlador, servicio y modelo/repositorio) para garantizar separación de responsabilidades, escalabilidad y mantenibilidad.

## Tecnologías principales

- Java 21
- Spring Boot
- Spring WebFlux
- Spring Data JPA / R2DBC
- PostgreSQL
- Eureka
- Spring Cloud Config
- RabbitMQ
- Lombok
- OpenAPI (Swagger)


## Funcionalidades principales

### Para clientes
- Agregar productos al carrito
- Eliminar productos del carrito
- Visualizar carrito con detalles
- Vaciar carrito
- Realizar compra (crear orden)

### Para administradores
- Consultar todos los carritos
- Consultar carritos abandonados
- Buscar carrito por ID

## Seguridad y autenticación

Todos los endpoints requieren el header `X-Auth-User-Id` con el ID del usuario autenticado.

## Configuración

La configuración se gestiona vía Spring Cloud Config. Ver `bootstrap.yml` y `application.yml`.

## Ejecución local

1. Instalar Java 21 y PostgreSQL
2. Configurar los microservicios de Auth e Inventario
3. Ejecutar:
	 ```
	 ./mvnw spring-boot:run
	 ```


## Documentación OpenAPI

La documentación interactiva de la API está disponible en:

[Swagger UI](http://localhost:8090/webjars/swagger-ui/index.html#/)

