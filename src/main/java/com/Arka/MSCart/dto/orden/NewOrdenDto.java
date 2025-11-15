package com.Arka.MSCart.dto.orden;

import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewOrdenDto {
    private Long idUsuario;
    private List<NewOrdenProductoDto> productos;
}
