package com.Arka.MSCart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {

    private Long productoId;
    private String nombreProducto; // Nombre del producto del MS de Inventario
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal; // cantidad * precioUnitario
}