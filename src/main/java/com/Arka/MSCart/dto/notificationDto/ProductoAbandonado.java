package com.Arka.MSCart.dto.notificationDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoAbandonado {
    private String nombreProducto;
    private int cantidad;
    private double precioUnitario;

}
