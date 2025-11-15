package com.Arka.MSCart.dto.notificationDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarritoAbandonado {
    private String nombreCliente;
    private String emailCliente;
    private String urlLogin;
    private List<ProductoAbandonado> productos;
}
