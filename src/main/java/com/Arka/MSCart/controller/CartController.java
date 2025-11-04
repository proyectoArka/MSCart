package com.Arka.MSCart.controller;

import com.Arka.MSCart.dto.AddItemRequestDTO;
import com.Arka.MSCart.dto.CartDTO;
import com.Arka.MSCart.dto.CartItemDTO; // DTO de respuesta que definimos antes
import com.Arka.MSCart.exception.CartNotFoundException;
import com.Arka.MSCart.exception.OutOfStockException;
import com.Arka.MSCart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Endpoint para agregar o actualizar la cantidad de un producto en el carrito.
     * * @param userId: Identificador del usuario inyectado por el API Gateway (e.g., el email).
     * @param requestDTO: Contiene el productId y la cantidad a agregar.
     * @return Mono<CartItemDTO>: El detalle del ítem actualizado.
     */
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CartItemDTO> addItemToCart(
            // 1. EXTRAER EL USER ID DEL HEADER
            @RequestHeader("X-Auth-User-Id") Long userId,
            // 2. RECIBIR EL CUERPO DE LA PETICIÓN
            @Valid @RequestBody AddItemRequestDTO requestDTO) {

        return cartService.addProductToCart(
                        userId,
                        requestDTO.getProductId(),
                        requestDTO.getQuantity()
                )
                // 3. MAPEO Y MANEJO DE ERRORES
                .map(cartDetail -> {
                    // Aquí deberías mapear CartDetail a CartItemDTO.
                    // Por simplicidad, se omiten los campos del MS de Inventario.
                    return CartItemDTO.builder()
                            .productoId(cartDetail.getProductoId())
                            .cantidad(cartDetail.getCantidad())
                            .precioUnitario(cartDetail.getPrecioUnitario())
                            .subtotal(cartDetail.getPrecioUnitario().multiply(
                                    new java.math.BigDecimal(cartDetail.getCantidad())))
                            .build();
                })
                // Manejo de errores específicos del servicio
                .onErrorResume(e -> {
                    if (e instanceof OutOfStockException) {
                        // Si el error es por falta de stock, devuelve 409 Conflict
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
                    }
                    // Otros errores internos devuelven 500
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar la petición: " + e.getMessage()));
                });
    }

    /**
     * Endpoint para obtener el carrito activo del usuario.
     * @param userId: Identificador del usuario inyectado por el API Gateway.
     * @return Mono<CartDTO>: El carrito completo.
     */
    @GetMapping
    public Mono<CartDTO> getCart(
            @RequestHeader("X-Auth-User-Id") Long userId) {
        System.out.println("Obteniendo carrito para el usuario ID: " + userId);

        return cartService.getCart(userId)
                .onErrorResume(e -> {
                    if (e instanceof CartNotFoundException) {
                        // Si el carrito no existe, se devuelve 404 Not Found
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()));
                    }
                    // Otros errores internos
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al obtener el carrito."));
                });
    }
}