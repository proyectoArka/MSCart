package com.Arka.MSCart.client;

import com.Arka.MSCart.dto.orden.NewOrdenDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * Cliente para comunicación con el Microservicio de Órdenes
 * Capa de Acceso a Datos - Comunicación Externa
 */
@Component
public class OrdenClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${ms.orden.baseUri}")
    private String baseUriOrden;

    @Value("${ms.orden.uriPath}")
    private String uriPathOrden;

    public OrdenClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    // Crear una nueva orden en el microservicio de órdenes
    public Mono<Void> crearOrden(NewOrdenDto newOrdenDto) {
        String uriString = UriComponentsBuilder
                .fromUriString(baseUriOrden)
                .path(uriPathOrden)
                .toUriString();

        return webClientBuilder.build()
                .post()
                .uri(uriString)
                .bodyValue(newOrdenDto)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(ex -> System.err.println("Error conectando MSOrden: " + ex.getMessage()));
    }
}

