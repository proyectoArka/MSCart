package com.Arka.MSCart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInCartDto {
    private Long id;
    private Long productoId;
    private String nombre;
    private String descripcion;
    private Long cantidad;
    private int precioUnitario;
    private int precioTotal;
}
