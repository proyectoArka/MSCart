package com.Arka.MSCart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("carrito")
public class Cart {
    @Id
    @Column("id")
    private Long id;
    @Column("userid")
    private Long userId;
    @Column("createdat")
    private LocalDateTime createdAt;
    @Column("estado")
    private boolean estado;
    @Column("ultimo_movimiento")
    private LocalDateTime ultimoMovimiento;
    @Column("numero_productos")
    private Long numeroProductos;
    @Column("emailenviado")
    private boolean emailEnviado;
}