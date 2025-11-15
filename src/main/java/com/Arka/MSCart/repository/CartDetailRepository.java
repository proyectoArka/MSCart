package com.Arka.MSCart.repository;

import com.Arka.MSCart.model.Cart;
import com.Arka.MSCart.model.CartDetail;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartDetailRepository extends ReactiveCrudRepository<CartDetail, Long> {

    /**
     * Retorna todos los detalles (ítems) asociados a un carrito específico.
     * Retorna un Flux<CartDetail> ya que un carrito puede tener N ítems.
     */
    Flux<CartDetail> findByCarritoId(Long carritoId);

    /**
     * Busca un ítem específico de un producto dentro de un carrito.
     */
    Mono<CartDetail> findByCarritoIdAndProductoId(Long carritoId, Long productoId);
    Mono<CartDetail> save(CartDetail cart);
    Flux<CartDetail> findAllByCarritoId(Long carritoId);
    Mono<Long> countByCarritoId(Long carritoId);
}