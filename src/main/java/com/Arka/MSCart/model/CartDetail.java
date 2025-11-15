package com.Arka.MSCart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("carrito_detalle")
public class CartDetail {

    @Id
    @Column("id")
    private Long id;
    @Column("carrito_id")
    private Long carritoId;
    @Column("producto_id")
    private Long productoId;
    @Column("cantidad")
    private Long cantidad;
    @Column("precio_total")
    private int precioTotal;
}