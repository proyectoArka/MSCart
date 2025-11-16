package com.Arka.MSCart.service;

import com.Arka.MSCart.client.AuthClient;
import com.Arka.MSCart.client.InventarioClient;
import com.Arka.MSCart.dto.CartWithProductsDto;
import com.Arka.MSCart.dto.ConsultProductInventarioDto;
import com.Arka.MSCart.dto.ProductInCartDto;
import com.Arka.MSCart.exception.CarritoNoEncontradoException;
import com.Arka.MSCart.exception.ProductoNoEncontradoException;
import com.Arka.MSCart.exception.StockInsuficienteException;
import com.Arka.MSCart.model.Cart;
import com.Arka.MSCart.model.CartDetail;
import com.Arka.MSCart.repository.CartDetailRepository;
import com.Arka.MSCart.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Servicio de Carrito para Clientes
 * Capa de Lógica de Negocio
 */
@Service
public class CartCustomerService {

    private static final Logger log = LoggerFactory.getLogger(CartCustomerService.class);

    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final InventarioClient inventarioClient;
    private final AuthClient authClient;

    public CartCustomerService(CartRepository cartRepository,
                              CartDetailRepository cartDetailRepository,
                              InventarioClient inventarioClient,
                              AuthClient authClient) {
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.inventarioClient = inventarioClient;
        this.authClient = authClient;
    }


    // Agrega un producto al carrito del usuario
    public Mono<CartWithProductsDto> addProductToCart(Long userId, Long productId, Long quantity) {
        return inventarioClient.consultarProducto(productId)
                .flatMap(stockPrice -> {
                    // Validación de negocio: cantidad válida
                    if (quantity <= 0) {
                        return Mono.error(StockInsuficienteException.cantidadInvalida(quantity));
                    }

                    // Validación de negocio: stock suficiente
                    Integer stockInventario = stockPrice.getStock();
                    if (stockInventario == null || stockInventario == 0) {
                        return Mono.error(StockInsuficienteException.sinStock(productId));
                    }
                    if (stockInventario < quantity) {
                        return Mono.error(StockInsuficienteException.conDetalles(productId, stockInventario, quantity));
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
                                            // Actualizar contadores y totales del carrito
                                            .flatMap(savedDetail ->
                                                    // Obtener todos los detalles del carrito para calcular totales
                                                    cartDetailRepository.findAllByCarritoId(cart.getId())
                                                            .collectList()
                                                            .flatMap(allDetails -> {
                                                                // Calcular número de productos diferentes
                                                                Long numeroProductos = (long) allDetails.size();

                                                                // Calcular total de unidades (suma de cantidades)
                                                                Long totalUnidades = allDetails.stream()
                                                                        .mapToLong(CartDetail::getCantidad)
                                                                        .sum();

                                                                // Calcular precio total (suma de precios totales)
                                                                Integer precioTotal = allDetails.stream()
                                                                        .mapToInt(CartDetail::getPrecioTotal)
                                                                        .sum();

                                                                // Actualizar el carrito
                                                                cart.setUltimoMovimiento(LocalDateTime.now());
                                                                cart.setNumeroProductos(numeroProductos);
                                                                cart.setTotalUnidades(totalUnidades);
                                                                cart.setPrecioTotal(precioTotal);
                                                                cart.setEstado(true);
                                                                cart.setEmailEnviado(false);

                                                                return cartRepository.save(cart).thenReturn(savedDetail);
                                                            })
                                            )
                            );
                })
                .then(Mono.defer(() -> getCartWithProducts(userId)));
    }

    // Consulta la información de un producto en inventario
    public Mono<ConsultProductInventarioDto> consultarProductoInventario(Long productoId) {
        return inventarioClient.consultarProducto(productoId);
    }

    /**
     * Obtiene el carrito del usuario con toda la información de productos y datos del usuario
     */
    public Mono<CartWithProductsDto> getCartWithProducts(Long userId) {
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(
                        CarritoNoEncontradoException.paraUsuario(userId)))
                .flatMap(cart ->
                    // Consultar información del usuario en paralelo con productos
                    Mono.zip(
                        // 1. Obtener información del usuario
                        authClient.consultarUsuario(userId)
                                .onErrorResume(ex -> {
                                    log.warn("Error consultando usuario {}: {}", userId, ex.getMessage());
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

    // Elimina un producto del carrito del usuario
    public Mono<CartWithProductsDto> removeProductFromCart(Long userId, Long productId) {
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(CarritoNoEncontradoException.paraUsuario(userId)))
                .flatMap(cart ->
                        cartDetailRepository.findByCarritoIdAndProductoId(cart.getId(), productId)
                                .switchIfEmpty(Mono.error(ProductoNoEncontradoException.enCarrito(productId)))
                                .flatMap(cartDetail ->
                                        cartDetailRepository.delete(cartDetail)
                                                .then(
                                                        // Recalcular totales después de eliminar
                                                        cartDetailRepository.findAllByCarritoId(cart.getId())
                                                                .collectList()
                                                                .flatMap(allDetails -> {
                                                                    Long numeroProductos = (long) allDetails.size();

                                                                    Long totalUnidades = allDetails.stream()
                                                                            .mapToLong(CartDetail::getCantidad)
                                                                            .sum();

                                                                    Integer precioTotal = allDetails.stream()
                                                                            .mapToInt(CartDetail::getPrecioTotal)
                                                                            .sum();

                                                                    cart.setNumeroProductos(numeroProductos);
                                                                    cart.setTotalUnidades(totalUnidades);
                                                                    cart.setPrecioTotal(precioTotal);
                                                                    cart.setUltimoMovimiento(LocalDateTime.now());
                                                                    cart.setEstado(true);

                                                                    return cartRepository.save(cart).thenReturn(cart);
                                                                })
                                                )
                                )
                )
                .flatMap(cart -> getCartWithProducts(userId))
                .doOnError(ex -> log.error("Error en removeProductFromCart userId={} productId={} -> {}", userId, productId, ex.toString()));
    }

    // Muestra el carrito del usuario con productos
    public Mono<CartWithProductsDto> viewCart(Long userId) {
        return getCartWithProducts(userId)
                .doOnError(ex -> log.error("Error en viewCart userId={} -> {}", userId, ex.toString()));
    }

    // Limpia el carrito del usuario
    public Mono<CartWithProductsDto> clearCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(CarritoNoEncontradoException.paraUsuario(userId)))
                .flatMap(cart ->
                        cartDetailRepository.findAllByCarritoId(cart.getId())
                                .flatMap(cartDetailRepository::delete)
                                .then(Mono.defer(() -> {
                                        cart.setNumeroProductos(0L);
                                        cart.setTotalUnidades(0L);
                                        cart.setPrecioTotal(0);
                                        cart.setUltimoMovimiento(LocalDateTime.now());
                                        cart.setEstado(true);
                                        return cartRepository.save(cart).thenReturn(cart);
                                }))
                )
                .flatMap(c -> getCartWithProducts(userId))
                .doOnError(ex -> log.error("Error en clearCart userId={} -> {}", userId, ex.toString()));
    }

}