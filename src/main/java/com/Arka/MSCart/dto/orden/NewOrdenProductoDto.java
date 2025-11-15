package com.Arka.MSCart.dto.orden;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewOrdenProductoDto {
    private Long productoId;
    private Long cantidad;
}
