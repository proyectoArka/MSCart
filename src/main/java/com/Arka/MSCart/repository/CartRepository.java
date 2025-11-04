package com.Arka.MSCart.repository;

import com.Arka.MSCart.model.Cart;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CartRepository extends ReactiveCrudRepository<Cart, Long> {
    Mono<Cart> findByUserId(Long userId);
}