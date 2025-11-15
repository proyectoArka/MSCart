package com.Arka.MSCart.service;

import com.Arka.MSCart.dto.CartWithProductsDto;
import com.Arka.MSCart.dto.orden.NewOrdenDto;
import com.Arka.MSCart.dto.orden.NewOrdenProductoDto;
import com.Arka.MSCart.repository.CartDetailRepository;
import com.Arka.MSCart.repository.CartRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
public class NewOrdenService {

    //private static final Logger log = LoggerFactory.getLogger(NewOrdenService.class);

    private final WebClient.Builder webClientBuilder;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final CartCustomerService cartCustomerService;
    public NewOrdenService(WebClient.Builder webClientBuilder,
                           CartRepository cartRepository,
                           CartDetailRepository cartDetailRepository,
                           CartCustomerService cartCustomerService) {
        this.webClientBuilder = webClientBuilder;
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.cartCustomerService = cartCustomerService;
    }

    @Value("${ms.orden.baseUri}")
    private String baseUriOrden;

    @Value("${ms.orden.uriPath}")
    private String uriPathOrden;

    public Mono<CartWithProductsDto> crearNuevaOrden(Long userId) {
        return cartRepository.findByUserId(userId)
                // Si no se encuentra el carrito, lanzar una excepción personalizada
                .switchIfEmpty(reactor.core.publisher.Mono.error(
                        new com.Arka.MSCart.exception.ProductoNoEncontradoException("Carrito no encontrado para userId " + userId)))
                .flatMap(cart -> {
                    if (cart.getNumeroProductos() == 0) {
                        // IMPORTANT: devolver el Mono.error para que se propague la excepción
                        return Mono.error(new com.Arka.MSCart.exception.CarritoVacioException(
                                "El carrito con el id " + userId + " está vacío. No se puede crear una orden."));
                    }
                    return cartDetailRepository.findAllByCarritoId(cart.getId())
                            .map(detail -> NewOrdenProductoDto.builder()
                                    .productoId(detail.getProductoId())
                                    .cantidad(detail.getCantidad())
                                    .build()
                            )
                            .collectList()
                            .map(productos -> {
                                NewOrdenDto ordenDto = NewOrdenDto.builder()
                                        .idUsuario(userId)
                                        .productos(productos)
                                        .build();

                                // ============================================================
                                // LOGS PARA IMPRIMIR EN TERMINAL - Fácil de comentar/descomentar
                                // ============================================================
                                //log.info("=================================================");
                                //log.info("NUEVA ORDEN CREADA");
                                //log.info("=================================================");
                                //log.info("ID Usuario: {}", userId);
                                //log.info("Productos en la orden:");
                                //productos.forEach(producto ->
                                //    log.info("  - Producto ID: {}, Cantidad: {}",
                                //            producto.getProductoId(), producto.getCantidad())
                                //);
                                //log.info("Total de productos: {}", productos.size());
                                //log.info("=================================================");
                                // ============================================================
                                //log.info("DTO Final a enviar a MSOrden: {}", ordenDto);
                                return ordenDto;
                            }).flatMap(ordenDto -> ConectarMSOrden(ordenDto)
                                    .thenReturn(ordenDto));

                })
                .then(Mono.defer(() ->
                        cartCustomerService.getCartWithProducts(userId)
                                .doOnSuccess(cartView ->
                                        cartRepository.deleteByUserId(userId).subscribe()
                                )
                ));
    }

        public Mono<Void> ConectarMSOrden(NewOrdenDto newOrdenDto) {
        String uriString = UriComponentsBuilder
                .fromUriString(baseUriOrden)  // lb://MSOrden
                .path(uriPathOrden)           // /api/v1/ordenes/neworden
                .toUriString();

        return webClientBuilder.build()
                .post()
                .uri(uriString)
                .bodyValue(newOrdenDto)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(ex -> System.err.println("Error conectando MSOrden: " + ex.getMessage()));
        }

}
