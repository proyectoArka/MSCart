package com.Arka.MSCart.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Data // Genera getters, setters, toString, equals, hashCode (Lombok)
@Builder // Patrón Builder para creación (Lombok)
@NoArgsConstructor
@AllArgsConstructor
@Table("carrito") // Mapea a la tabla 'carrito' en PostgreSQL
public class Cart {

    @Id // Clave primaria
    private Long id;

    // Identificador único del usuario (viene del JWT, e.g., el email)
    private Long userId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}