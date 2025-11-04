package com.Arka.MSCart.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /**
     * @LoadBalanced: Permite usar el nombre del servicio de Eureka (e.g., "inventory-service")
     * en lugar de una URL y puerto específicos.
     * Esto es crucial para la comunicación entre microservicios.
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}