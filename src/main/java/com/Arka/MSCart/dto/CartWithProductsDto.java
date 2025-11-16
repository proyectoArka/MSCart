package com.Arka.MSCart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO del carrito con lista completa de productos e información del usuario")
public class CartWithProductsDto {

    @Schema(description = "ID único del carrito", example = "1")
    private Long cartId;

    @Schema(description = "ID del usuario propietario del carrito", example = "123")
    private Long userId;

    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez García")
    private String nombreUsuario;

    @Schema(description = "Dirección del usuario", example = "Calle Principal 123, Bogotá")
    private String direccionUsuario;

    @Schema(description = "Teléfono del usuario", example = "+57 300 123 4567")
    private String telefonoUsuario;

    @Schema(description = "Estado del carrito (activo/inactivo)", example = "activo", allowableValues = {"activo", "inactivo"})
    private String estadoCarrito;

    @Schema(description = "Número total de productos en el carrito", example = "3")
    private Long numeroProductos;

    @Schema(description = "Total de unidades de todos los productos (suma de cantidades)", example = "5")
    private Long totalUnidades;

    @Schema(description = "Precio total del carrito (suma de todos los precios)", example = "2500")
    private Integer precioTotal;

    @Schema(description = "Fecha y hora de creación del carrito", example = "2025-11-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Fecha y hora del último movimiento en el carrito", example = "2025-11-15T11:45:00")
    private LocalDateTime ultimoMovimiento;

    @Schema(description = "Lista de productos en el carrito")
    private List<ProductInCartDto> products;
}
