package com.Arka.MSCart.service;

import com.Arka.MSCart.dto.AdminDto.CartDto;
import com.Arka.MSCart.dto.AdminDto.ConsultUserInAuthDto;
import com.Arka.MSCart.dto.CartWithProductsDto;
import com.Arka.MSCart.dto.ProductInCartDto;
import com.Arka.MSCart.exception.UsuarioNoEncontradoException;
import com.Arka.MSCart.model.Cart;
import com.Arka.MSCart.repository.CartDetailRepository;
import com.Arka.MSCart.repository.CartRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class CartAdminService {

    private static final Logger log = LoggerFactory.getLogger(CartAdminService.class);

    private final CartRepository cartRepository;
    private final WebClient.Builder webClientBuilder;
    private final CartDetailRepository cartDetailRepository;
    private final CartCustomerService cartCustomerService;

    @Value("${ms.auth.baseUri}")
    private String authBaseUri;

    @Value("${ms.auth.uriPath}")
    private String authUriPath;

    // tiempo de avandono de carrito en minutos
    @Value("${ms.cart.abandonCart.time}")
    private int cartAbandonTimeMinutes;

    // tiempo en minutos de cada cuanto se ejecuta la funcion para determinar si un carrito esta abandonado
    @Value("${ms.cart.functionAbandonCart.time}")
    private int functionAbandonCartTimeMinutes;

    public CartAdminService(CartRepository cartRepository,
                            WebClient.Builder webClientBuilder,
                            CartDetailRepository cartDetailRepository,
                            CartCustomerService cartCustomerService) {
        this.cartRepository = cartRepository;
        this.webClientBuilder = webClientBuilder;
        this.cartDetailRepository = cartDetailRepository;
        this.cartCustomerService = cartCustomerService;
    }

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

    public Mono<ConsultUserInAuthDto> getConsultUserInAuth(Long cartId) {
        String fullPath = authUriPath.contains("{") ? authUriPath : authUriPath + "/{id}";

        URI uri = UriComponentsBuilder
                .fromHttpUrl(authBaseUri)
                .path(fullPath)
                .buildAndExpand(cartId)
                .toUri();

        return webClientBuilder.build()
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(ConsultUserInAuthDto.class)
                .onErrorResume(WebClientResponseException.InternalServerError.class, ex ->
                        Mono.error(new UsuarioNoEncontradoException(
                                "el usuario con ID " + cartId + " no encontrado"
                        ))
                );
    }

    public Flux<CartDto> getAllCartsAdmin() {
        return cartRepository.findAll()
                .flatMap(cart -> getConsultUserInAuth(cart.getUserId())
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

    public Flux<CartDto> getAbandonedCarts() {
        return cartRepository.findAll()
                .filter(cart -> !cart.isEstado())
                .flatMap(cart ->
                        getConsultUserInAuth(cart.getUserId())
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

    public Mono<CartWithProductsDto> getCartWithProductsIdCart(Long cartId) {
        return cartRepository.findById(cartId)
                // Si no se encuentra el carrito, lanzar una excepción personalizada
                .switchIfEmpty(reactor.core.publisher.Mono.error(
                        new com.Arka.MSCart.exception.ProductoNoEncontradoException("Carrito no encontrado con el id " + cartId)))

                // Por cada carrito encontrado, obtener sus detalles y la información de los productos
                .flatMap(cart ->
                        cartDetailRepository.findAllByCarritoId(cart.getId())
                                .flatMap(detail ->
                                        cartCustomerService.consultarProductoInventario(detail.getProductoId())
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

}

