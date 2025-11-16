# 🛒 MSCart - Microservicio de Gestión de Carritos de Compra

![Java](https://img.shields.io/badge/Java-21-orange?style=flat&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen?style=flat&logo=spring)
![WebFlux](https://img.shields.io/badge/WebFlux-Reactive-blue?style=flat)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat&logo=postgresql)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)

Microservicio reactivo para la gestión de carritos de compra en el ecosistema de e-commerce **Arka**. Desarrollado con Spring Boot 3.5.7 y WebFlux para garantizar alta concurrencia y escalabilidad.

---

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Arquitectura](#-arquitectura)
- [Tecnologías](#-tecnologías)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Endpoints de la API](#-endpoints-de-la-api)
  - [Endpoints para Clientes](#endpoints-para-clientes)
  - [Endpoints para Administradores](#endpoints-para-administradores)
- [Modelos de Datos](#-modelos-de-datos)
- [Documentación Swagger](#-documentación-swagger)
- [Testing](#-testing)
- [Integración con Otros Microservicios](#-integración-con-otros-microservicios)
- [Contribuir](#-contribuir)
- [Licencia](#-licencia)

---

## ✨ Características

### Para Clientes
- ✅ **Agregar productos al carrito** con validación de stock en tiempo real
- ✅ **Eliminar productos** del carrito de forma individual
- ✅ **Visualizar carrito** con información completa del usuario y productos
- ✅ **Vaciar carrito** completamente
- ✅ **Realizar compra** (checkout) y crear órdenes automáticamente
- ✅ **Cálculo automático** de totales (unidades y precio)

### Para Administradores
- ✅ **Consultar todos los carritos** del sistema
- ✅ **Identificar carritos abandonados** para campañas de recuperación
- ✅ **Buscar carrito específico** por ID con detalles completos
- ✅ **Monitoreo en tiempo real** del estado de los carritos

### Funcionalidades Técnicas
- 🚀 **Programación Reactiva** con Spring WebFlux (Project Reactor)
- 🔄 **Comunicación asíncrona** con otros microservicios
- 📊 **Totales precalculados** (totalUnidades, precioTotal)
- 🛡️ **Manejo robusto de errores** con excepciones personalizadas
- 📧 **Notificaciones automáticas** de carritos abandonados
- 🔍 **Detección automática** de carritos inactivos
- 📝 **Documentación OpenAPI 3.0** completa

---

## 🏛️ Arquitectura

Este microservicio sigue el patrón de **Arquitectura de Tres Capas (Three-Tier Architecture)** para garantizar separación de responsabilidades y mantenibilidad.

```
┌─────────────────────────────────────────────────────────┐
│                CAPA DE PRESENTACIÓN                      │
│  (Controllers, DTOs, Documentación OpenAPI)              │
├─────────────────────────────────────────────────────────┤
│  • CartCustomerController - Operaciones de clientes     │
│  • CardAdminController - Operaciones administrativas    │
│  • DTOs con validaciones @Valid                         │
│  • Documentación Swagger/OpenAPI 3.0                    │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│              CAPA DE LÓGICA DE NEGOCIO                   │
│  (Services, Validaciones, Orquestación)                  │
├─────────────────────────────────────────────────────────┤
│  • CartCustomerService - Lógica de carrito de clientes  │
│  • CartAdminService - Lógica administrativa             │
│  • NewOrdenService - Creación de órdenes                │
│  • NotificationService - Notificaciones automáticas     │
│  • Validaciones de negocio (stock, cantidades, etc.)    │
│  • Cálculo de totales automático                        │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│            CAPA DE ACCESO A DATOS                        │
│  (Repositories, Clients, Comunicación Externa)           │
├─────────────────────────────────────────────────────────┤
│  REPOSITORIES (Base de Datos):                          │
│  • CartRepository - CRUD de carritos                    │
│  • CartDetailRepository - CRUD de detalles              │
│                                                          │
│  CLIENTS (Microservicios):                              │
│  • InventarioClient → MS-Inventario                     │
│  • AuthClient → MS-Autenticación                        │
│  • OrdenClient → MS-Órdenes                             │
└─────────────────────────────────────────────────────────┘
```

### Patrón de Diseño
- **Repository Pattern** para acceso a datos
- **Service Layer** para lógica de negocio
- **DTO Pattern** para transferencia de datos
- **Factory Method** para excepciones personalizadas
- **Client Pattern** para comunicación con microservicios

---

## 🛠️ Tecnologías

### Backend Framework
- **Java 21** - Lenguaje de programación
- **Spring Boot 3.5.7** - Framework principal
- **Spring WebFlux** - Programación reactiva no bloqueante
- **Spring Data R2DBC** - Acceso reactivo a base de datos
- **Project Reactor** - Biblioteca de programación reactiva

### Base de Datos
- **PostgreSQL 15** - Base de datos relacional
- **R2DBC PostgreSQL** - Driver reactivo para PostgreSQL

### Comunicación entre Microservicios
- **Spring Cloud Netflix Eureka** - Descubrimiento de servicios
- **Spring Cloud Config** - Configuración centralizada
- **WebClient** - Cliente HTTP reactivo

### Documentación
- **SpringDoc OpenAPI 3.0** - Generación automática de documentación
- **Swagger UI** - Interfaz interactiva de la API

### Testing
- **JUnit 5** - Framework de testing
- **Mockito** - Mocking para tests unitarios
- **Reactor Test** - Testing de flujos reactivos (StepVerifier)

### Utilidades
- **Lombok** - Reducción de código boilerplate
- **Validation API** - Validación de datos con anotaciones
- **SLF4J/Logback** - Logging

---

## 📦 Instalación

### Prerrequisitos
```bash
# Java 21
java -version

# Maven 3.9+
mvn -version

# PostgreSQL 15+
psql --version
```

### 1. Clonar el Repositorio
```bash
git clone https://github.com/arka-ecommerce/MSCart.git
cd MSCart
```

### 2. Configurar Base de Datos
```sql
-- Crear base de datos
CREATE DATABASE mscart_db;

-- Crear tablas
CREATE TABLE carrito (
    id BIGSERIAL PRIMARY KEY,
    userid BIGINT NOT NULL,
    createdat TIMESTAMP NOT NULL,
    estado BOOLEAN DEFAULT TRUE,
    ultimo_movimiento TIMESTAMP,
    numero_productos BIGINT DEFAULT 0,
    total_unidades BIGINT DEFAULT 0,
    precio_total INTEGER DEFAULT 0,
    emailenviado BOOLEAN DEFAULT FALSE
);

CREATE TABLE carrito_detalle (
    id BIGSERIAL PRIMARY KEY,
    carrito_id BIGINT NOT NULL REFERENCES carrito(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL,
    cantidad BIGINT NOT NULL,
    precio_total INTEGER NOT NULL
);

-- Índices para mejor rendimiento
CREATE INDEX idx_carrito_userid ON carrito(userid);
CREATE INDEX idx_carrito_estado ON carrito(estado);
CREATE INDEX idx_detalle_carrito ON carrito_detalle(carrito_id);
```

### 3. Configurar application.yml
```yaml
spring:
  application:
    name: MSCart
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/mscart_db
    username: your_username
    password: your_password

server:
  port: 8080

# Configuración de otros microservicios
ms:
  inventario:
    baseUri: lb://MSInventario
    uriPath: /api/v1/productos/stockprice
  auth:
    baseUri: lb://MSAuthentication
    uriPath: /api/v1/auth/consuluser
  orden:
    baseUri: lb://MSOrden
    uriPath: /api/v1/ordenes/neworden
```

### 4. Compilar y Ejecutar
```bash
# Compilar
mvn clean install

# Ejecutar
mvn spring-boot:run

# O ejecutar el JAR
java -jar target/MSCart-0.0.1-SNAPSHOT.jar
```

La aplicación estará disponible en: `http://localhost:8080`

---

## ⚙️ Configuración

### Variables de Entorno
```bash
# Base de datos
DB_HOST=localhost
DB_PORT=5432
DB_NAME=mscart_db
DB_USER=postgres
DB_PASSWORD=password

# Servidor
SERVER_PORT=8080

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka

# Config Server
SPRING_CLOUD_CONFIG_URI=http://localhost:8888
```

### Perfiles de Spring
```bash
# Desarrollo
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Producción
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## 🌐 Endpoints de la API

### Endpoints para Clientes

#### 1. Agregar Producto al Carrito
```http
POST /api/v1/carts/agregarproducto
```

**Headers:**
```
X-Auth-User-Id: 123
Content-Type: application/json
```

**Request Body:**
```json
{
  "productId": 100,
  "quantity": 2
}
```

**Response (201 Created):**
```json
{
  "cartId": 1,
  "userId": 123,
  "nombreUsuario": "Juan Pérez García",
  "direccionUsuario": "Calle Principal 123, Bogotá",
  "telefonoUsuario": "+57 300 123 4567",
  "estadoCarrito": "activo",
  "numeroProductos": 2,
  "totalUnidades": 5,
  "precioTotal": 2500,
  "createdAt": "2025-11-15T10:30:00",
  "ultimoMovimiento": "2025-11-15T11:45:00",
  "products": [
    {
      "id": 1,
      "productoId": 100,
      "nombre": "Laptop Dell XPS 15",
      "descripcion": "Laptop profesional de alto rendimiento",
      "cantidad": 1,
      "precioUnitario": 1500.00,
      "precioTotal": 1500
    },
    {
      "id": 2,
      "productoId": 200,
      "nombre": "Mouse Logitech MX Master",
      "descripcion": "Mouse ergonómico inalámbrico",
      "cantidad": 4,
      "precioUnitario": 250.00,
      "precioTotal": 1000
    }
  ]
}
```

**Errores:**
```json
// 400 - Stock insuficiente
{
  "status": 400,
  "message": "Stock insuficiente para el producto con ID 100. Stock disponible: 5, solicitado: 10"
}

// 404 - Producto no encontrado
{
  "status": 404,
  "message": "Producto con ID 999 no encontrado en inventario"
}
```

---

#### 2. Ver Carrito del Usuario
```http
GET /api/v1/carts/vercarrito
```

**Headers:**
```
X-Auth-User-Id: 123
```

**Response (200 OK):**
```json
{
  "cartId": 1,
  "userId": 123,
  "nombreUsuario": "Juan Pérez García",
  "direccionUsuario": "Calle Principal 123, Bogotá",
  "telefonoUsuario": "+57 300 123 4567",
  "estadoCarrito": "activo",
  "numeroProductos": 3,
  "totalUnidades": 8,
  "precioTotal": 3750,
  "createdAt": "2025-11-15T10:30:00",
  "ultimoMovimiento": "2025-11-15T14:20:00",
  "products": [
    {
      "id": 1,
      "productoId": 100,
      "nombre": "Laptop Dell XPS 15",
      "cantidad": 1,
      "precioUnitario": 1500.00,
      "precioTotal": 1500
    },
    {
      "id": 2,
      "productoId": 200,
      "nombre": "Mouse Logitech",
      "cantidad": 4,
      "precioUnitario": 250.00,
      "precioTotal": 1000
    },
    {
      "id": 3,
      "productoId": 300,
      "nombre": "Teclado Mecánico",
      "cantidad": 3,
      "precioUnitario": 750.00,
      "precioTotal": 2250
    }
  ]
}
```

---

#### 3. Eliminar Producto del Carrito
```http
DELETE /api/v1/carts/eliminarproducto/{productId}
```

**Headers:**
```
X-Auth-User-Id: 123
```

**Response (200 OK):**
```json
{
  "cartId": 1,
  "userId": 123,
  "nombreUsuario": "Juan Pérez García",
  "estadoCarrito": "activo",
  "numeroProductos": 2,
  "totalUnidades": 5,
  "precioTotal": 2250,
  "products": [...]
}
```

---

#### 4. Vaciar Carrito
```http
DELETE /api/v1/carts/vaciarcarrito
```

**Headers:**
```
X-Auth-User-Id: 123
```

**Response (200 OK):**
```json
{
  "cartId": 1,
  "userId": 123,
  "nombreUsuario": "Juan Pérez García",
  "estadoCarrito": "activo",
  "numeroProductos": 0,
  "totalUnidades": 0,
  "precioTotal": 0,
  "products": []
}
```

---

#### 5. Realizar Compra (Checkout)
```http
GET /api/v1/carts/realizarcompra
```

**Headers:**
```
X-Auth-User-Id: 123
```

**Response (200 OK):**
```json
{
  "cartId": 1,
  "userId": 123,
  "nombreUsuario": "Juan Pérez García",
  "estadoCarrito": "activo",
  "numeroProductos": 2,
  "totalUnidades": 3,
  "precioTotal": 1650,
  "products": [...]
}
```

**Proceso:**
1. Valida que el carrito no esté vacío
2. Crea una orden en el microservicio de órdenes
3. Retorna el carrito antes de eliminarlo
4. Elimina el carrito del usuario

---

### Endpoints para Administradores

#### 1. Obtener Todos los Carritos
```http
GET /api/v1/cartsadmin/all
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "userName": "Juan Pérez",
    "numeroProductos": 3,
    "estado": true,
    "createdAt": "2025-11-15T10:30:00",
    "ultimoMovimiento": "2025-11-15T11:45:00"
  },
  {
    "id": 2,
    "userName": "María García",
    "numeroProductos": 1,
    "estado": false,
    "createdAt": "2025-11-14T09:20:00",
    "ultimoMovimiento": "2025-11-14T09:25:00"
  }
]
```

---

#### 2. Obtener Carritos Abandonados
```http
GET /api/v1/cartsadmin/cartabandonados
```

**Response (200 OK):**
```json
[
  {
    "id": 2,
    "userName": "María García",
    "numeroProductos": 1,
    "estado": false,
    "createdAt": "2025-11-14T09:20:00",
    "ultimoMovimiento": "2025-11-14T09:25:00"
  },
  {
    "id": 5,
    "userName": "Carlos Ruiz",
    "numeroProductos": 4,
    "estado": false,
    "createdAt": "2025-11-13T15:10:00",
    "ultimoMovimiento": "2025-11-13T15:30:00"
  }
]
```

---

#### 3. Buscar Carrito por ID
```http
GET /api/v1/cartsadmin/cartid/{id}
```

**Response (200 OK):**
```json
{
  "cartId": 1,
  "userId": 123,
  "nombreUsuario": "Juan Pérez García",
  "direccionUsuario": "Calle Principal 123, Bogotá",
  "telefonoUsuario": "+57 300 123 4567",
  "estadoCarrito": "activo",
  "numeroProductos": 2,
  "totalUnidades": 5,
  "precioTotal": 2500,
  "products": [...]
}
```

---

## 📊 Modelos de Datos

### Cart (Carrito)
```java
{
  "id": Long,                    // ID único del carrito
  "userId": Long,                // ID del usuario propietario
  "createdAt": LocalDateTime,    // Fecha de creación
  "estado": Boolean,             // true=activo, false=abandonado
  "ultimoMovimiento": LocalDateTime,  // Última actualización
  "numeroProductos": Long,       // Productos diferentes
  "totalUnidades": Long,         // Suma de cantidades
  "precioTotal": Integer,        // Suma de precios
  "emailEnviado": Boolean        // Notificación enviada
}
```

### CartDetail (Detalle del Carrito)
```java
{
  "id": Long,                    // ID único del detalle
  "carritoId": Long,             // FK al carrito
  "productoId": Long,            // ID del producto
  "cantidad": Long,              // Cantidad del producto
  "precioTotal": Integer         // Precio total (cantidad * precio)
}
```

### CartWithProductsDto (Response)
```java
{
  "cartId": Long,
  "userId": Long,
  "nombreUsuario": String,
  "direccionUsuario": String,
  "telefonoUsuario": String,
  "estadoCarrito": String,       // "activo" | "inactivo"
  "numeroProductos": Long,
  "totalUnidades": Long,
  "precioTotal": Integer,
  "createdAt": LocalDateTime,
  "ultimoMovimiento": LocalDateTime,
  "products": [ProductInCartDto]
}
```

---

## 📚 Documentación Swagger

### Acceso a Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON
```
http://localhost:8080/v3/api-docs
```

### OpenAPI YAML
```
http://localhost:8080/v3/api-docs.yaml
```

### Captura de Pantalla de Swagger UI
La interfaz de Swagger incluye:
- ✅ Lista completa de endpoints agrupados por tags
- ✅ Ejemplos de request y response
- ✅ Botón "Try it out" para probar cada endpoint
- ✅ Esquemas de datos detallados
- ✅ Códigos de respuesta HTTP documentados

---

## 🧪 Testing

### Ejecutar Tests Unitarios
```bash
# Todos los tests
mvn test

# Tests específicos
mvn test -Dtest=AuthClientTest
mvn test -Dtest=CartCustomerServiceTest
```

### Cobertura de Tests
```bash
# Con JaCoCo
mvn test jacoco:report

# Ver reporte
open target/site/jacoco/index.html
```

### Ejemplo de Test Unitario
```java
@ExtendWith(MockitoExtension.class)
class CartCustomerServiceTest {
    
    @Mock
    private CartRepository cartRepository;
    
    @Mock
    private InventarioClient inventarioClient;
    
    @InjectMocks
    private CartCustomerService service;
    
    @Test
    void deberiaAgregarProductoAlCarrito() {
        // Given
        Long userId = 1L;
        Long productId = 100L;
        
        // When
        Mono<CartWithProductsDto> result = 
            service.addProductToCart(userId, productId, 2L);
        
        // Then
        StepVerifier.create(result)
            .expectNextMatches(cart -> 
                cart.getUserId().equals(userId)
            )
            .verifyComplete();
    }
}
```

---

## 🔗 Integración con Otros Microservicios

### MS-Inventario
**Propósito:** Consultar stock y precios de productos

**Endpoint:** `GET /api/v1/productos/stockprice/{id}`

**Uso:**
```java
inventarioClient.consultarProducto(productId)
    .flatMap(stockPrice -> {
        // Validar stock disponible
        // Obtener precio actual
    });
```

---

### MS-Autenticación
**Propósito:** Obtener información del usuario

**Endpoint:** `GET /api/v1/auth/consuluser/{id}`

**Uso:**
```java
authClient.consultarUsuario(userId)
    .map(userDto -> {
        // Obtener nombre, dirección, teléfono
    });
```

---

### MS-Órdenes
**Propósito:** Crear nueva orden de compra

**Endpoint:** `POST /api/v1/ordenes/neworden`

**Uso:**
```java
ordenClient.crearOrden(newOrdenDto)
    .then(/* eliminar carrito */);
```

---

## 🔐 Seguridad

### Autenticación
- Header `X-Auth-User-Id` requerido en todos los endpoints de clientes
- Validación de usuario a través de MS-Autenticación

### Validaciones
- ✅ Stock disponible antes de agregar productos
- ✅ Cantidades mayores a 0
- ✅ Existencia de productos en catálogo
- ✅ Carrito no vacío antes de crear orden

---

## 📈 Monitoreo y Métricas

### Actuator Endpoints
```bash
# Health check
curl http://localhost:8080/actuator/health

# Métricas
curl http://localhost:8080/actuator/metrics

# Info
curl http://localhost:8080/actuator/info
```

---

## 🚀 Deployment

### Docker
```dockerfile
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/MSCart-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# Build
docker build -t mscart:latest .

# Run
docker run -p 8080:8080 \
  -e DB_HOST=postgres \
  -e DB_PORT=5432 \
  mscart:latest
```

### Docker Compose
```yaml
version: '3.8'
services:
  mscart:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=mscart_db
    depends_on:
      - postgres
  
  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=mscart_db
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=password
    ports:
      - "5432:5432"
```

---

## 📝 Logs

### Niveles de Log
```yaml
logging:
  level:
    com.Arka.MSCart: DEBUG
    org.springframework.r2dbc: DEBUG
    reactor.netty: INFO
```

### Ejemplo de Logs
```
2025-11-15 10:30:00 INFO  CartCustomerService - Usuario 123 agregó producto 100 al carrito
2025-11-15 10:30:05 DEBUG CartRepository - Guardando carrito: Cart(id=1, userId=123)
2025-11-15 10:30:10 WARN  InventarioClient - Error consultando producto 999: Producto no encontrado
```

<div align="center">
  
**⭐ Si este proyecto te fue útil, dale una estrella en GitHub ⭐**

Made with ❤️ by the Arka Team

</div>

