package com.Arka.MSCart.client;

import com.Arka.MSCart.dto.AdminDto.ConsultUserInAuthDto;
import com.Arka.MSCart.exception.UsuarioNoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Cliente para comunicación con el Microservicio de Autenticación
 * Capa de Acceso a Datos - Comunicación Externa
 */
@Component
public class AuthClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${ms.auth.baseUri}")
    private String authBaseUri;

    @Value("${ms.auth.uriPath}")
    private String authUriPath;

    public AuthClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    // Consultar usuario en el microservicio de autenticación por ID
    public Mono<ConsultUserInAuthDto> consultarUsuario(Long userId) {
        String fullPath = authUriPath.contains("{") ? authUriPath : authUriPath + "/{id}";

        URI uri = UriComponentsBuilder
                .fromUriString(authBaseUri)
                .path(fullPath)
                .buildAndExpand(userId)
                .toUri();

        return webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(ConsultUserInAuthDto.class)
                .onErrorResume(WebClientResponseException.InternalServerError.class, ex ->
                        Mono.error(UsuarioNoEncontradoException.conId(userId))
                );
    }
}

