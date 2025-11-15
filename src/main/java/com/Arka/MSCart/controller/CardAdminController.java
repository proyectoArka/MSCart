package com.Arka.MSCart.controller;

import com.Arka.MSCart.dto.AdminDto.CartDto;
import com.Arka.MSCart.dto.CartWithProductsDto;
import com.Arka.MSCart.service.CartAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/cartsadmin")
@RequiredArgsConstructor
public class CardAdminController {

    private final CartAdminService cartAdminService;

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<CartDto> obtenerTodosLosCarritos() {
        return cartAdminService.getAllCartsAdmin();
    }

    @GetMapping("/cartabandonados")
    public Flux<CartDto> obtenerCarritosAbandonados() {
        return cartAdminService.getAbandonedCarts();
    }

    @GetMapping("/cartid/{id}")
    public Mono<CartWithProductsDto> buscarCarritoPorIdCart(@PathVariable Long id) {
        return cartAdminService.getCartWithProductsIdCart(id);
    }
}
