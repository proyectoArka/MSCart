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
public class ProductoStockDTO {

    // Usamos el ID para asegurar la referencia
    private Long id;
    private String nombre;
    private Integer stock;
    private BigDecimal precio;
}