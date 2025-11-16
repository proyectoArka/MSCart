package com.Arka.MSCart.dto.AdminDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información del usuario consultada desde el servicio de autenticación")
public class ConsultUserInAuthDto {

    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez García")
    private String name;

    @Schema(description = "Correo electrónico del usuario", example = "juan.perez@example.com")
    private String email;

    @Schema(description = "Dirección del usuario", example = "Calle Principal 123, Bogotá")
    private String direccion;

    @Schema(description = "Teléfono del usuario", example = "+57 300 123 4567")
    private String telefono;
}
