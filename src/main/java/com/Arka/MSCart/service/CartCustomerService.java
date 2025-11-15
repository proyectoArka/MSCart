package com.Arka.MSCart.service;

import com.Arka.MSCart.dto.CartWithProductsDto;
import com.Arka.MSCart.dto.ConsultProductInventarioDto;
import com.Arka.MSCart.dto.ProductInCartDto;
import com.Arka.MSCart.exception.ProductoNoEncontradoException;
import com.Arka.MSCart.model.Cart;
import com.Arka.MSCart.model.CartDetail;
import com.Arka.MSCart.repository.CartDetailRepository;
import com.Arka.MSCart.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;

@Service
public class CartCustomerService {

    private static final Logger log = LoggerFactory.getLogger(CartCustomerService.class);

    private final WebClient.Builder webClientBuilder;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;

    @Value("${ms.inventario.baseUri}")
    private String baseUri;

    @Value("${ms.inventario.uriPath}")
    private String uriPath;

    public CartCustomerService(WebClient.Builder webClientBuilder, CartRepository cartRepository, CartDetailRepository cartDetailRepository) {
        this.webClientBuilder = webClientBuilder;
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
    }

    public Mono<CartWithProductsDto> addProductToCart(Long userId, Long productId, Long quantity) {
        return consultarProductoInventario(productId)
                .flatMap(stockPrice -> {
                    Integer stockInventario = stockPrice.getStock();
                    if (stockInventario == null || stockInventario < quantity || quantity <= 0) {
                        return Mono.error(new IllegalStateException("No hay stock suficiente o cantidad inválida"));
                    }
                    // Obtener o crear el carrito del usuario
                    return cartRepository.findByUserId(userId)
                            .switchIfEmpty(Mono.defer(() -> {
                                Cart newCart = Cart.builder()
                                        .userId(userId)
                                        .estado(true)
                                        .createdAt(LocalDateTime.now())
                                        .ultimoMovimiento(LocalDateTime.now())
                                        .numeroProductos(0L)
                                        .emailEnviado(false)
                                        .build();
                                return cartRepository.save(newCart);
                            }))
                            .flatMap(cart ->
                                    // Verificar si el producto ya está en el carrito
                                    cartDetailRepository.findByCarritoIdAndProductoId(cart.getId(), productId)
                                            .flatMap(cartDetail -> {
                                                // Si ya existe, actualizar la cantidad
                                                cartDetail.setCantidad(quantity);
                                                cartDetail.setPrecioTotal((int) (quantity * stockPrice.getPrice()));
                                                return cartDetailRepository.save(cartDetail);
                                            })
                                            // Si no existe, crear un nuevo detalle de carrito
                                            .switchIfEmpty(Mono.defer(() -> {
                                                CartDetail newProductDetail = CartDetail.builder()
                                                        .carritoId(cart.getId())
                                                        .productoId(productId)
                                                        .cantidad(quantity)
                                                        .precioTotal((int) (quantity * stockPrice.getPrice()))
                                                        .build();
                                                return cartDetailRepository.save(newProductDetail);
                                            }))
                                            // Actualizar la fecha del último producto agregado al carrito y numero de productos
                                            .flatMap(savedDetail ->
                                                    cartDetailRepository.countByCarritoId(cart.getId())
                                                            .defaultIfEmpty(0L)
                                                            .flatMap(count -> {
                                                                cart.setUltimoMovimiento(LocalDateTime.now());
                                                                cart.setNumeroProductos(count);
                                                                cart.setEstado(true);
                                                                cart.setEmailEnviado(false);
                                                                return cartRepository.save(cart).thenReturn(savedDetail);
                                                            })
                                            )

                            );
                })
                .then(Mono.defer(() -> getCartWithProducts(userId)));
    }

    public Mono<ConsultProductInventarioDto> consultarProductoInventario(Long productoId) {
        String fullPath = uriPath.contains("{") ? uriPath : uriPath + "/{id}";

        URI uri = UriComponentsBuilder
                .fromHttpUrl(baseUri)
                .path(fullPath)
                .buildAndExpand(productoId)
                .toUri();

        return webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(ConsultProductInventarioDto.class)
                .onErrorResume(WebClientResponseException.InternalServerError.class, ex ->
                        Mono.error(new ProductoNoEncontradoException(
                                "Producto con ID " + productoId + " no encontrado"
                        ))
                );
    }

    public Mono<CartWithProductsDto> getCartWithProducts(Long userId) {
        return cartRepository.findByUserId(userId)
                // Si no se encuentra el carrito, lanzar una excepción personalizada
                .switchIfEmpty(reactor.core.publisher.Mono.error(
                        new com.Arka.MSCart.exception.ProductoNoEncontradoException("Carrito no encontrado para userId " + userId)))
                .flatMap(cart ->

                    cartDetailRepository.findAllByCarritoId(cart.getId())
                            .flatMap(detail ->
                                    consultarProductoInventario(detail.getProductoId())
                                        .map(inv -> ProductInCartDto.builder()
                                                .id(detail.getId())
                                                .productoId(detail.getProductoId())
                                                .nombre(inv.getNombre())
                                                .descripcion(inv.getDescripcion())
                                                .cantidad(detail.getCantidad())
                                                .precioUnitario(inv.getPrice())
                                                .precioTotal(detail.getPrecioTotal())
                                                .build())
                                            .onErrorResume(ex -> Mono.just(ProductInCartDto.builder()
                                                    .id(detail.getId())
                                                    .productoId(detail.getProductoId())
                                                    .nombre("error en inventario nombre no disponible")
                                                    .descripcion("error en inventario descripción no disponible")
                                                    .cantidad(detail.getCantidad())
                                                    .precioUnitario(0)
                                                    .precioTotal(detail.getPrecioTotal())
                                                    .build()))
                            )
                            .collectList()
                            .map(products -> {
                                CartWithProductsDto cartDto = new CartWithProductsDto();
                                    cartDto.setCartId(cart.getId());
                                    cartDto.setUserId(cart.getUserId());
                                    cartDto.setNumeroProductos(cart.getNumeroProductos());
                                    cartDto.setCreatedAt(cart.getCreatedAt());
                                    cartDto.setEstado(cart.isEstado());
                                    cartDto.setUltimoMovimiento(cart.getUltimoMovimiento());
                                    cartDto.setProducts(products);
                                return cartDto;
                            })
                );
    }

    public Mono<CartWithProductsDto> removeProductFromCart(Long userId, Long productId) {
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new ProductoNoEncontradoException("Carrito no encontrado para userId " + userId)))
                .flatMap(cart ->
                        cartDetailRepository.findByCarritoIdAndProductoId(cart.getId(), productId)
                                .switchIfEmpty(Mono.error(new ProductoNoEncontradoException("Producto con ID " + productId + " no encontrado en el carrito")))
                                .flatMap(cartDetail ->
                                        cartDetailRepository.delete(cartDetail)
                                                .then(
                                                        cartDetailRepository.countByCarritoId(cart.getId())
                                                                .defaultIfEmpty(0L)
                                                                .flatMap(count -> {
                                                                    cart.setNumeroProductos(count);
                                                                    cart.setUltimoMovimiento(LocalDateTime.now());
                                                                    cart.setEstado(true);
                                                                    return cartRepository.save(cart).thenReturn(cart);
                                                                })
                                                )
                                )
                )

                // después de la eliminación, obtener y devolver el carrito actualizado
                .flatMap(cart -> getCartWithProducts(userId))
                .doOnError(ex -> log.error("Error en removeProductFromCart userId={} productId={} -> {}", userId, productId, ex.toString()));
    }

    public Mono<CartWithProductsDto> viewCart(Long userId) {
        return getCartWithProducts(userId)
                .doOnError(ex -> log.error("Error en viewCart userId={} -> {}", userId, ex.toString()));
    }

    public Mono<CartWithProductsDto> clearCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new ProductoNoEncontradoException("Carrito no encontrado para userId " + userId)))
                .flatMap(cart ->
                        cartDetailRepository.findAllByCarritoId(cart.getId())
                                .flatMap(cartDetailRepository::delete)
                                .then(Mono.defer(() -> {
                                        cart.setNumeroProductos(0L);
                                        cart.setUltimoMovimiento(LocalDateTime.now());
                                        cart.setEstado(true);
                                        return cartRepository.save(cart).thenReturn(cart);
                                }))
                )
                // después de limpiar, obtener y devolver el carrito actualizado (vacío)
                .flatMap(c -> getCartWithProducts(userId))
                .doOnError(ex -> log.error("Error en clearCart userId={} -> {}", userId, ex.toString()));
    }

}