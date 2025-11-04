package com.Arka.MSCart.service;

import com.Arka.MSCart.dto.CartDTO;
import com.Arka.MSCart.dto.CartItemDTO;
import com.Arka.MSCart.dto.ProductoStockDTO;
import com.Arka.MSCart.exception.CartNotFoundException;
import com.Arka.MSCart.model.Cart;
import com.Arka.MSCart.model.CartDetail;
import com.Arka.MSCart.repository.CartDetailRepository;
import com.Arka.MSCart.repository.CartRepository;
import com.Arka.MSCart.exception.OutOfStockException; // Necesitas crear esta excepción
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor // Inyecta las dependencias (Lombok)
@Transactional // R2DBC soporta transacciones reactivas
public class CartService {

    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final WebClient.Builder webClientBuilder; // WebClient inyectado


    /**
     * Lógica para agregar un producto al carrito.
     * Orquestación reactiva:
     * 1. Obtener o crear el carrito del usuario.
     * 2. Consultar Inventario MS (WebClient) para stock.
     * 3. Guardar el detalle del carrito (R2DBC).
     */
    public Mono<CartDetail> addProductToCart(Long userId, Long productId, int quantity) {

        // 1. Obtener o crear el carrito. (Mono<Cart>)
        Mono<Cart> cartMono = cartRepository.findByUserId(userId)
                .switchIfEmpty(createCart(userId)); // Si no existe, crea uno nuevo.

        // 2. Obtener detalles y stock del Microservicio de Inventario (WebClient)
        Mono<ProductoStockDTO> productMono = webClientBuilder.build()
                .get()
                .uri("lb://NSInventario/api/v1/productos/buscar/{id}", productId)
                .retrieve()
                .bodyToMono(ProductoStockDTO.class)
                .switchIfEmpty(Mono.error(new RuntimeException("Producto no encontrado en Inventario")));

        // 3. Combinar las dos operaciones asíncronas y aplicar la lógica
        return Mono.zip(cartMono, productMono)
                .flatMap(tuple -> {
                    Cart cart = tuple.getT1();
                    ProductoStockDTO product = tuple.getT2();

                    // Validar Stock (Criterio de Aceptación HU4)
                    if (product.getStock() < quantity) {
                        return Mono.error(new OutOfStockException(
                                "Stock insuficiente para " + product.getNombre() +
                                        ". Disponible: " + product.getStock()
                        ));
                    }

                    // Buscar si el producto ya está en el carrito para actualizar
                    return cartDetailRepository
                            .findByCarritoIdAndProductoId(cart.getId(), productId)
                            .defaultIfEmpty(CartDetail.builder().carritoId(cart.getId()).productoId(productId).build())
                            .flatMap(detail -> {
                                // Lógica de actualización de cantidad
                                detail.setCantidad(detail.getCantidad() != null ? detail.getCantidad() + quantity : quantity);
                                detail.setPrecioUnitario(product.getPrecio()); // Actualizar precio al momento

                                // Guardar el detalle de forma reactiva (R2DBC)
                                return cartDetailRepository.save(detail);
                            });
                });
    }

    /**
     * Método auxiliar reactivo para crear un nuevo carrito.
     */
    private Mono<Cart> createCart(Long userId) {
        Cart newCart = Cart.builder()
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return cartRepository.save(newCart);
    }


    /**
     * Obtiene el carrito del usuario, resuelve los nombres/stock de los productos
     * con el MS de Inventario (Fan-Out) y calcula el total.
     * @param userId: Identificador único del usuario.
     * @return Mono<CartDTO>: El carrito completo listo para la presentación.
     */
    public Mono<CartDTO> getCart(Long userId) {

        // 1. Buscar el carrito por userId, crear uno nuevo si no existe
        Mono<Cart> cartMono = cartRepository.findByUserId(userId)
                .switchIfEmpty(
                        Mono.defer(() -> {
                            Cart newCart = Cart.builder()
                                    .userId(userId)
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .build();
                            return cartRepository.save(newCart);
                        })
                );

        return cartMono
                .flatMap(cart -> {

                    // 2. Obtener los detalles (ítems) del carrito (R2DBC)
                    Flux<CartDetail> detailsFlux = cartDetailRepository.findByCarritoId(cart.getId());

                    // 3. Transformar cada CartDetail a un CartItemDTO con datos del Inventario MS
                    Flux<CartItemDTO> itemDTOFlux = detailsFlux.flatMap(detail -> {

                        Mono<ProductoStockDTO> productMono = webClientBuilder.build()
                                .get()
                                .uri("lb://MSInventario/api/v1/productos/buscar/{id}", detail.getProductoId())
                                .retrieve()
                                .bodyToMono(ProductoStockDTO.class)
                                .onErrorResume(e -> {
                                    return Mono.just(ProductoStockDTO.builder()
                                            .nombre("[Producto Eliminado]")
                                            .stock(0)
                                            .precio(detail.getPrecioUnitario())
                                            .build());
                                });

                        return productMono.map(productDto -> {
                            BigDecimal subtotal = detail.getPrecioUnitario()
                                    .multiply(new BigDecimal(detail.getCantidad()));

                            return CartItemDTO.builder()
                                    .productoId(detail.getProductoId())
                                    .nombreProducto(productDto.getNombre())
                                    .cantidad(detail.getCantidad())
                                    .precioUnitario(detail.getPrecioUnitario())
                                    .subtotal(subtotal)
                                    .build();
                        });
                    });

                    // 4. Recolectar items y calcular total
                    return itemDTOFlux.collectList()
                            .map(items -> {
                                BigDecimal total = items.stream()
                                        .map(CartItemDTO::getSubtotal)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                return CartDTO.builder()
                                        .cartId(cart.getId())
                                        .userId(cart.getUserId())  // Asegúrate que CartDTO.userId sea Long
                                        .items(items)
                                        .total(total)
                                        .build();
                            });
                });
    }
}