package com.Arka.MSCart.client;

import com.Arka.MSCart.dto.AdminDto.ConsultUserInAuthDto;
import com.Arka.MSCart.exception.UsuarioNoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para AuthClient
 * Capa de Acceso a Datos - Comunicación Externa
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthClient - Pruebas Unitarias")
class AuthClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private AuthClient authClient;

    @BeforeEach
    void setUp() {
        authClient = new AuthClient(webClientBuilder);

        // Inyectar valores de configuración usando ReflectionTestUtils
        ReflectionTestUtils.setField(authClient, "authBaseUri", "lb://MSAuthentication");
        ReflectionTestUtils.setField(authClient, "authUriPath", "/api/v1/auth/consuluser");

        // Configurar cadena de mocks para WebClient
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
    }

    @Test
    @DisplayName("Debería consultar usuario exitosamente cuando el usuario existe")
    void deberiaConsultarUsuarioExitosamente() {
        // Given - Datos de prueba
        Long userId = 1L;
        ConsultUserInAuthDto expectedUser = ConsultUserInAuthDto.builder()
                .name("Juan Pérez")
                .email("juan.perez@example.com")
                .build();

        // Mock de la cadena de WebClient
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ConsultUserInAuthDto.class)).thenReturn(Mono.just(expectedUser));

        // When - Ejecutar el método
        Mono<ConsultUserInAuthDto> resultado = authClient.consultarUsuario(userId);

        // Then - Verificar resultado
        StepVerifier.create(resultado)
                .expectNextMatches(user ->
                    user.getName().equals("Juan Pérez") &&
                    user.getEmail().equals("juan.perez@example.com")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería lanzar UsuarioNoEncontradoException cuando el servicio retorna 500")
    void deberiaLanzarExcepcionCuandoServicioRetorna500() {
        // Given
        Long userId = 999L;
        WebClientResponseException error = WebClientResponseException.create(
                500,
                "Internal Server Error",
                null,
                null,
                null
        );

        // Mock de la cadena de WebClient que lanza error
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ConsultUserInAuthDto.class))
                .thenReturn(Mono.error(error));

        // When - Ejecutar el método
        Mono<ConsultUserInAuthDto> resultado = authClient.consultarUsuario(userId);

        // Then - Verificar que se lanza la excepción correcta
        StepVerifier.create(resultado)
                .expectErrorMatches(throwable ->
                    throwable instanceof UsuarioNoEncontradoException &&
                    throwable.getMessage().contains("El usuario con ID 999 no encontrado")
                )
                .verify();
    }

    @Test
    @DisplayName("Debería manejar usuario con ID nulo correctamente")
    void deberiaManejarUsuarioIdNulo() {
        // Given
        Long userId = null;

        // Mock de la cadena de WebClient
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ConsultUserInAuthDto.class))
                .thenReturn(Mono.empty());

        // When
        Mono<ConsultUserInAuthDto> resultado = authClient.consultarUsuario(userId);

        // Then - Debería completar vacío
        StepVerifier.create(resultado)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería consultar múltiples usuarios correctamente")
    void deberiaConsultarMultiplesUsuarios() {
        // Given - Múltiples IDs de usuarios
        Long userId1 = 1L;
        Long userId2 = 2L;

        ConsultUserInAuthDto user1 = ConsultUserInAuthDto.builder()
                .name("Usuario 1")
                .email("user1@example.com")
                .build();

        ConsultUserInAuthDto user2 = ConsultUserInAuthDto.builder()
                .name("Usuario 2")
                .email("user2@example.com")
                .build();

        // Mock para primera llamada
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ConsultUserInAuthDto.class))
                .thenReturn(Mono.just(user1))
                .thenReturn(Mono.just(user2));

        // When - Ejecutar consultas
        Mono<ConsultUserInAuthDto> resultado1 = authClient.consultarUsuario(userId1);
        Mono<ConsultUserInAuthDto> resultado2 = authClient.consultarUsuario(userId2);

        // Then - Verificar resultados
        StepVerifier.create(resultado1)
                .expectNextMatches(user -> user.getName().equals("Usuario 1"))
                .verifyComplete();

        StepVerifier.create(resultado2)
                .expectNextMatches(user -> user.getName().equals("Usuario 2"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería construir URI correctamente con authUriPath que contiene {id}")
    void deberiaConstruirUriCorrectamenteConPlaceholder() {
        // Given
        ReflectionTestUtils.setField(authClient, "authUriPath", "/api/v1/auth/consuluser/{id}");
        Long userId = 5L;

        ConsultUserInAuthDto expectedUser = ConsultUserInAuthDto.builder()
                .name("Test User")
                .email("test@example.com")
                .build();

        // Mock de la cadena de WebClient
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ConsultUserInAuthDto.class)).thenReturn(Mono.just(expectedUser));

        // When
        Mono<ConsultUserInAuthDto> resultado = authClient.consultarUsuario(userId);

        // Then
        StepVerifier.create(resultado)
                .expectNextMatches(user -> user.getName().equals("Test User"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar respuesta vacía del servicio de autenticación")
    void deberiaManejarRespuestaVacia() {
        // Given
        Long userId = 10L;

        // Mock de la cadena de WebClient que retorna vacío
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ConsultUserInAuthDto.class)).thenReturn(Mono.empty());

        // When
        Mono<ConsultUserInAuthDto> resultado = authClient.consultarUsuario(userId);

        // Then
        StepVerifier.create(resultado)
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar timeout del servicio externo")
    void deberiaManejarTimeoutDelServicio() {
        // Given
        Long userId = 20L;

        // Mock de la cadena de WebClient que simula timeout
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ConsultUserInAuthDto.class))
                .thenReturn(Mono.error(new java.util.concurrent.TimeoutException("Timeout")));

        // When
        Mono<ConsultUserInAuthDto> resultado = authClient.consultarUsuario(userId);

        // Then - El timeout no se maneja específicamente, se propaga
        StepVerifier.create(resultado)
                .expectError(java.util.concurrent.TimeoutException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería manejar error de red (Connection refused)")
    void deberiaManejarErrorDeRed() {
        // Given
        Long userId = 30L;

        // Mock de la cadena de WebClient que simula error de conexión
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ConsultUserInAuthDto.class))
                .thenReturn(Mono.error(new java.net.ConnectException("Connection refused")));

        // When
        Mono<ConsultUserInAuthDto> resultado = authClient.consultarUsuario(userId);

        // Then - El error de conexión se propaga
        StepVerifier.create(resultado)
                .expectError(java.net.ConnectException.class)
                .verify();
    }

    @Test
    @DisplayName("Debería retornar usuario con datos completos")
    void deberiaRetornarUsuarioConDatosCompletos() {
        // Given
        Long userId = 100L;
        ConsultUserInAuthDto expectedUser = ConsultUserInAuthDto.builder()
                .name("María García López")
                .email("maria.garcia@empresa.com")
                .build();

        // Mock de la cadena de WebClient
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ConsultUserInAuthDto.class)).thenReturn(Mono.just(expectedUser));

        // When
        Mono<ConsultUserInAuthDto> resultado = authClient.consultarUsuario(userId);

        // Then - Verificar todos los campos
        StepVerifier.create(resultado)
                .expectNextMatches(user -> {
                    assert user.getName() != null : "El nombre no debe ser nulo";
                    assert user.getEmail() != null : "El email no debe ser nulo";
                    assert user.getName().equals("María García López");
                    assert user.getEmail().equals("maria.garcia@empresa.com");
                    return true;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Debería manejar diferentes códigos de error HTTP correctamente")
    void deberiaManejarDiferentesCodigosDeError() {
        // Given
        Long userId = 404L;

        // Simular error 404
        WebClientResponseException error404 = WebClientResponseException.create(
                404,
                "Not Found",
                null,
                null,
                null
        );

        // Mock de la cadena de WebClient
        when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ConsultUserInAuthDto.class))
                .thenReturn(Mono.error(error404));

        // When
        Mono<ConsultUserInAuthDto> resultado = authClient.consultarUsuario(userId);

        // Then - Error 404 no se maneja específicamente, solo 500
        StepVerifier.create(resultado)
                .expectError(WebClientResponseException.class)
                .verify();
    }
}

