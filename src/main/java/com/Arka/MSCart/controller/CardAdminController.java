package com.Arka.MSCart.controller;

import com.Arka.MSCart.dto.AdminDto.CartDto;
import com.Arka.MSCart.dto.CartWithProductsDto;
import com.Arka.MSCart.service.CartAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/cartsadmin")
@RequiredArgsConstructor
@Tag(name = "Carrito de Compra - Admin", description = "Operaciones administrativas para gestión de carritos")
public class CardAdminController {

    private final CartAdminService cartAdminService;

    @Operation(
            summary = "Obtener todos los carritos del sistema",
            description = """
                    Retorna una lista completa de todos los carritos en el sistema.
                    Incluye información del usuario asociado a cada carrito.
                    
                    **Uso:** Monitoreo general del sistema y análisis de uso.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de carritos obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CartDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            [
                                              {
                                                "id": 1,
                                                "userName": "Juan Pérez",
                                                "numeroProductos": 3,
                                                "estado": true,
                                                "createdAt": "2025-11-15T10:30:00",
                                                "ultimoMovimiento": "2025-11-15T11:45:00"
                                              },
                                              {
                                                "id": 2,
                                                "userName": "María García",
                                                "numeroProductos": 1,
                                                "estado": false,
                                                "createdAt": "2025-11-14T09:20:00",
                                                "ultimoMovimiento": "2025-11-14T09:25:00"
                                              }
                                            ]
                                            """
                            )
                    )
            )
    })
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<CartDto> obtenerTodosLosCarritos() {
        return cartAdminService.getAllCartsAdmin();
    }

    @Operation(
            summary = "Obtener carritos abandonados",
            description = """
                    Retorna una lista de carritos marcados como abandonados (estado = false).
                    
                    **Criterios de abandono:**
                    - Carrito inactivo por más de X minutos (configurable)
                    - Estado automáticamente marcado como false
                    
                    **Uso:** Campañas de recuperación de carritos abandonados.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de carritos abandonados obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CartDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            [
                                              {
                                                "id": 2,
                                                "userName": "María García",
                                                "numeroProductos": 1,
                                                "estado": false,
                                                "createdAt": "2025-11-14T09:20:00",
                                                "ultimoMovimiento": "2025-11-14T09:25:00"
                                              }
                                            ]
                                            """
                            )
                    )
            )
    })
    @GetMapping("/cartabandonados")
    public Flux<CartDto> obtenerCarritosAbandonados() {
        return cartAdminService.getAbandonedCarts();
    }

    @Operation(
            summary = "Buscar carrito por ID de carrito",
            description = """
                    Retorna un carrito específico con todos sus productos y detalles.
                    
                    **Uso:** Soporte al cliente, auditoría, resolución de problemas.
                    """,
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID del carrito a consultar",
                            required = true,
                            example = "1"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Carrito encontrado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CartWithProductsDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "cartId": 1,
                                              "userId": 123,
                                              "nombreUsuario": "Juan Pérez García",
                                              "direccionUsuario": "Calle Principal 123, Bogotá",
                                              "telefonoUsuario": "+57 300 123 4567",
                                              "estadoCarrito": "activo",
                                              "numeroProductos": 2,
                                              "totalUnidades": 5,
                                              "precioTotal": 2500,
                                              "createdAt": "2025-11-15T10:30:00",
                                              "ultimoMovimiento": "2025-11-15T11:45:00",
                                              "products": [
                                                {
                                                  "id": 1,
                                                  "productoId": 100,
                                                  "nombre": "Laptop Dell XPS 15",
                                                  "cantidad": 1,
                                                  "precioUnitario": 1500.00,
                                                  "precioTotal": 1500
                                                },
                                                {
                                                  "id": 2,
                                                  "productoId": 200,
                                                  "nombre": "Mouse Logitech",
                                                  "cantidad": 4,
                                                  "precioUnitario": 250.00,
                                                  "precioTotal": 1000
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Carrito no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Carrito no encontrado con el ID 999"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/cartid/{id}")
    public Mono<CartWithProductsDto> buscarCarritoPorIdCart(@PathVariable Long id) {
        return cartAdminService.getCartWithProductsIdCart(id);
    }
}
