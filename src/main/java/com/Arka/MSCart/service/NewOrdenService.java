package com.Arka.MSCart.service;

import com.Arka.MSCart.client.OrdenClient;
import com.Arka.MSCart.dto.CartWithProductsDto;
import com.Arka.MSCart.dto.orden.NewOrdenDto;
import com.Arka.MSCart.dto.orden.NewOrdenProductoDto;
import com.Arka.MSCart.exception.CarritoNoEncontradoException;
import com.Arka.MSCart.exception.CarritoVacioException;
import com.Arka.MSCart.repository.CartDetailRepository;
import com.Arka.MSCart.repository.CartRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class NewOrdenService {

    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final CartCustomerService cartCustomerService;
    private final OrdenClient ordenClient;

    public NewOrdenService(CartRepository cartRepository,
                           CartDetailRepository cartDetailRepository,
                           CartCustomerService cartCustomerService,
                           OrdenClient ordenClient) {
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.cartCustomerService = cartCustomerService;
        this.ordenClient = ordenClient;
    }


    // Crear una nueva orden a partir del carrito del usuario
    public Mono<CartWithProductsDto> crearNuevaOrden(Long userId) {
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(
                        CarritoNoEncontradoException.paraUsuario(userId)))
                .flatMap(cart -> {
                    // Validación de negocio: el carrito no puede estar vacío
                    if (cart.getNumeroProductos() == 0) {
                        return Mono.error(CarritoVacioException.paraUsuario(userId));
                    }

                    // Construir DTO de orden con los productos del carrito
                    return cartDetailRepository.findAllByCarritoId(cart.getId())
                            .map(detail -> NewOrdenProductoDto.builder()
                                    .productoId(detail.getProductoId())
                                    .cantidad(detail.getCantidad())
                                    .build()
                            )
                            .collectList()
                            .map(productos -> NewOrdenDto.builder()
                                    .idUsuario(userId)
                                    .productos(productos)
                                    .build()
                            )
                            // Enviar orden al microservicio de órdenes
                            .flatMap(ordenDto -> ordenClient.crearOrden(ordenDto)
                                    .thenReturn(ordenDto));
                })
                // Después de crear la orden exitosamente, mostrar el carrito y luego eliminarlo
                .then(Mono.defer(() ->
                        cartCustomerService.getCartWithProducts(userId)
                                .doOnSuccess(cartView ->
                                        cartRepository.deleteByUserId(userId).subscribe()
                                )
                ));
    }

}
