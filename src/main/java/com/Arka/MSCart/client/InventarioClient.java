package com.Arka.MSCart.client;

import com.Arka.MSCart.dto.ConsultProductInventarioDto;
import com.Arka.MSCart.exception.ProductoNoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Cliente para comunicación con el Microservicio de Inventario
 * Capa de Acceso a Datos - Comunicación Externa
 */
@Component
public class InventarioClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${ms.inventario.baseUri}")
    private String baseUri;

    @Value("${ms.inventario.uriPath}")
    private String uriPath;

    public InventarioClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    // Consultar un producto en el inventario por su ID
    public Mono<ConsultProductInventarioDto> consultarProducto(Long productoId) {
        String fullPath = uriPath.contains("{") ? uriPath : uriPath + "/{id}";

        URI uri = UriComponentsBuilder
                .fromUriString(baseUri)
                .path(fullPath)
                .buildAndExpand(productoId)
                .toUri();

        return webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(ConsultProductInventarioDto.class)
                .onErrorResume(WebClientResponseException.InternalServerError.class, ex ->
                        Mono.error(ProductoNoEncontradoException.enInventario(productoId))
                );
    }
}

