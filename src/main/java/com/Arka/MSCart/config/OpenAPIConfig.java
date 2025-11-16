package com.Arka.MSCart.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI 3.0 para documentación de la API de Carritos
 * Capa de Presentación - Configuración de Documentación
 */
@Configuration
public class OpenAPIConfig {

    @Value("${spring.application.name:MSCart}")
    private String applicationName;

    @Bean
    public OpenAPI cartOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestión de Carritos de Compra - Arka E-commerce")
                        .description("""
                                API RESTful reactiva para la gestión de carritos de compra en el ecosistema Arka E-commerce.
                                
                                ## Funcionalidades Principales
                                
                                ### Para Clientes
                                - ✅ Agregar productos al carrito
                                - ✅ Eliminar productos del carrito
                                - ✅ Visualizar carrito con detalles de productos
                                - ✅ Vaciar carrito completo
                                - ✅ Realizar compra (crear orden)
                                
                                ### Para Administradores
                                - ✅ Consultar todos los carritos del sistema
                                - ✅ Consultar carritos abandonados
                                - ✅ Buscar carrito por ID
                                
                                ## Arquitectura
                                - **Framework:** Spring Boot 3.5.7 con Spring WebFlux (Reactivo)
                                - **Base de Datos:** PostgreSQL con R2DBC
                                - **Patrón:** Arquitectura de Tres Capas
                                - **Comunicación:** WebClient para microservicios
                                
                                ## Autenticación
                                Todos los endpoints requieren el header `X-Auth-User-Id` con el ID del usuario autenticado.
                                """)
                        .version("1.0.0")
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desarrollo Local"),
                        new Server()
                                .url("http://localhost:8093/api/v1/gateway")
                                .description("API Gateway - Desarrollo")
                ));
    }
}

