package com.Arka.MSCart.dto.notificationDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String destination;
    private String asunto;
    private String cuerpoMensaje;
    private String tipoEvento;
}
