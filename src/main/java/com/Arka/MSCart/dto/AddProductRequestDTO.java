package com.Arka.MSCart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AddProductRequestDTO {

    // AddProductRequestDTO.java
    @NotNull(message = "El productId no puede ser nulo")
    @Positive(message = "El productId debe ser positivo")
    private Long productId;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Long quantity;
}