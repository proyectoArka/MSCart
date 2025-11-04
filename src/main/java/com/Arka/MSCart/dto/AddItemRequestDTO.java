package com.Arka.MSCart.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
public class AddItemRequestDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer quantity;

}