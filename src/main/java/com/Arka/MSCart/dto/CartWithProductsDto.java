package com.Arka.MSCart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartWithProductsDto {
    private Long cartId;
    private Long userId;
    private Boolean estado;
    private Long numeroProductos;
    private LocalDateTime createdAt;
    private LocalDateTime ultimoMovimiento;
    private List<ProductInCartDto> products;
}
