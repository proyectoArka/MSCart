package com.Arka.MSCart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("carrito_detalle")
public class CartDetail {

    @Id
    private Long id;
    private Long carritoId;
    private Long productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
}