package com.Arka.MSCart.dto.AdminDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartDto {

    private Long cartId;
    private String nameUser;
    private Long numeroProductos;
    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime ultimoMovimiento;

}
