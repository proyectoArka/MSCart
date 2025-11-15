package com.Arka.MSCart.controller;

import com.Arka.MSCart.dto.AddProductRequestDTO;
import com.Arka.MSCart.dto.CartWithProductsDto;
import com.Arka.MSCart.service.CartCustomerService;
import com.Arka.MSCart.service.NewOrdenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartCustomerController {

    private final CartCustomerService cartService;
    private final NewOrdenService newOrdenService;

    @PostMapping("/agregarproducto")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CartWithProductsDto> addProductToCart(@RequestHeader("X-Auth-User-Id") Long userId,
                                       @Valid @RequestBody AddProductRequestDTO requestDTO) {
        return cartService.addProductToCart(userId, requestDTO.getProductId(), requestDTO.getQuantity());
    }

    @DeleteMapping("/eliminarproducto/{productId}")
    public Mono<CartWithProductsDto> removeProductFromCart(@RequestHeader("X-Auth-User-Id") Long userId,
                                                           @PathVariable Long productId) {
        return cartService.removeProductFromCart(userId, productId);
    }

    @GetMapping("/vercarrito")
    public Mono<CartWithProductsDto> viewCart(@RequestHeader("X-Auth-User-Id") Long userId) {
        return cartService.viewCart(userId);

    }

    @DeleteMapping("/vaciarcarrito")
    public Mono<CartWithProductsDto> clearCart(@RequestHeader("X-Auth-User-Id") Long userId) {
        return cartService.clearCart(userId);
    }

    @GetMapping("/realizarcompra")
    public Mono<CartWithProductsDto> checkoutCart(@RequestHeader("X-Auth-User-Id")Long userId) {
        return newOrdenService.crearNuevaOrden(userId);
    }

}
