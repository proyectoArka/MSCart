package com.Arka.MSCart.service;

import com.Arka.MSCart.client.AuthClient;
import com.Arka.MSCart.client.InventarioClient;
import com.Arka.MSCart.dto.AdminDto.CartDto;
import com.Arka.MSCart.dto.CartWithProductsDto;
import com.Arka.MSCart.dto.ProductInCartDto;
import com.Arka.MSCart.exception.CarritoNoEncontradoException;
import com.Arka.MSCart.model.Cart;
import com.Arka.MSCart.repository.CartDetailRepository;
import com.Arka.MSCart.repository.CartRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class CartAdminService {

    private static final Logger log = LoggerFactory.getLogger(CartAdminService.class);

    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final AuthClient authClient;
    private final InventarioClient inventarioClient;

    // Tiempo de abandono de carrito en minutos
    @Value("${ms.cart.abandonCart.time}")
    private int cartAbandonTimeMinutes;

    // Tiempo en minutos de cada cuanto se ejecuta la función para determinar si un carrito está abandonado
    @Value("${ms.cart.functionAbandonCart.time}")
    private int functionAbandonCartTimeMinutes;

    public CartAdminService(CartRepository cartRepository,
                            CartDetailRepository cartDetailRepository,
                            AuthClient authClient,
                            InventarioClient inventarioClient) {
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.authClient = authClient;
        this.inventarioClient = inventarioClient;
    }

    // Función que se ejecuta periódicamente para determinar si un carrito está abandonado
    @PostConstruct
    public void determinarSiUnCartEstaAbandonado() {
        Duration intervaloEjecucionFuncion = Duration.ofMinutes(functionAbandonCartTimeMinutes);
        Duration tiempoAbandonoCarrito = Duration.ofMinutes(cartAbandonTimeMinutes);

        Flux.interval(intervaloEjecucionFuncion, intervaloEjecucionFuncion, Schedulers.parallel())
                .flatMap(tick -> {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime cutoff = now.minus(tiempoAbandonoCarrito);

                    return cartRepository.findAll()
                            .filter(Cart::isEstado)
                            .filter(cart -> {
                                LocalDateTime last = cart.getUltimoMovimiento() != null ? cart.getUltimoMovimiento() : cart.getCreatedAt();
                                return last != null && last.isBefore(cutoff);
                            })
                            .flatMap(cart -> {
                                cart.setEstado(false);
                                return cartRepository.save(cart);
                            })
                            .then();
                })
                .doOnError(e -> log.error("Error en verificación de carritos abandonados", e))
                .subscribe();
    }

    // Obtiene todos los carritos con información de usuario
    public Flux<CartDto> getAllCartsAdmin() {
        return cartRepository.findAll()
                .flatMap(cart -> authClient.consultarUsuario(cart.getUserId())
                        .map(userDto -> new CartDto(
                                cart.getId(),
                                userDto.getName(),
                                cart.getNumeroProductos(),
                                cart.isEstado(),
                                cart.getCreatedAt(),
                                cart.getUltimoMovimiento()
                        ))
                );
    }

    // Obtiene todos los carritos abandonados con información de usuario
    public Flux<CartDto> getAbandonedCarts() {
        return cartRepository.findAll()
                .filter(cart -> !cart.isEstado())
                .flatMap(cart ->
                        authClient.consultarUsuario(cart.getUserId())
                            .map(userDto -> new CartDto(
                                cart.getId(),
                                userDto.getName(),
                                cart.getNumeroProductos(),
                                cart.isEstado(),
                                cart.getCreatedAt(),
                                cart.getUltimoMovimiento()
                        ))
                );
    }

    // Obtiene un carrito con sus productos por ID de carrito
    public Mono<CartWithProductsDto> getCartWithProductsIdCart(Long cartId) {
        return cartRepository.findById(cartId)
                .switchIfEmpty(Mono.error(
                        CarritoNoEncontradoException.conId(cartId)))
                .flatMap(cart ->
                    // Consultar información del usuario en paralelo con productos
                    Mono.zip(
                        // 1. Obtener información del usuario
                        authClient.consultarUsuario(cart.getUserId())
                                .onErrorResume(ex -> {
                                    log.warn("Error consultando usuario {} para carrito {}: {}",
                                            cart.getUserId(), cartId, ex.getMessage());
                                    return Mono.empty();
                                }),

                        // 2. Obtener productos del carrito
                        cartDetailRepository.findAllByCarritoId(cart.getId())
                                .flatMap(detail ->
                                        inventarioClient.consultarProducto(detail.getProductoId())
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
                    )
                    .map(tuple -> {
                        var userDto = tuple.getT1();
                        var products = tuple.getT2();

                        CartWithProductsDto cartDto = new CartWithProductsDto();
                        cartDto.setCartId(cart.getId());
                        cartDto.setUserId(cart.getUserId());

                        // Información del usuario
                        if (userDto != null) {
                            cartDto.setNombreUsuario(userDto.getName());
                            cartDto.setDireccionUsuario(userDto.getDireccion());
                            cartDto.setTelefonoUsuario(userDto.getTelefono());
                        } else {
                            cartDto.setNombreUsuario("Usuario no disponible");
                            cartDto.setDireccionUsuario("No disponible");
                            cartDto.setTelefonoUsuario("No disponible");
                        }

                        // Convertir estado booleano a texto
                        cartDto.setEstadoCarrito(cart.isEstado() ? "activo" : "inactivo");

                        cartDto.setNumeroProductos(cart.getNumeroProductos());
                        cartDto.setTotalUnidades(cart.getTotalUnidades());
                        cartDto.setPrecioTotal(cart.getPrecioTotal());
                        cartDto.setCreatedAt(cart.getCreatedAt());
                        cartDto.setUltimoMovimiento(cart.getUltimoMovimiento());
                        cartDto.setProducts(products);

                        return cartDto;
                    })
                );
    }

}

