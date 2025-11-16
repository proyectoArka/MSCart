package com.Arka.MSCart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Schema(description = "DTO para agregar un producto al carrito")
public class AddProductRequestDTO {

    @Schema(
            description = "ID del producto a agregar al carrito",
            example = "100",
            required = true,
            minimum = "1"
    )
    @NotNull(message = "El productId no puede ser nulo")
    @Positive(message = "El productId debe ser positivo")
    private Long productId;

    @Schema(
            description = "Cantidad del producto a agregar",
            example = "2",
            required = true,
            minimum = "1"
    )
    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Long quantity;
}