package com.Arka.MSCart.controller;

import com.Arka.MSCart.dto.AddProductRequestDTO;
import com.Arka.MSCart.dto.CartWithProductsDto;
import com.Arka.MSCart.service.CartCustomerService;
import com.Arka.MSCart.service.NewOrdenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@Tag(name = "Carrito de Compra - Cliente", description = "Operaciones de gestión de carrito para clientes")
public class CartCustomerController {

    private final CartCustomerService cartService;
    private final NewOrdenService newOrdenService;


    @Operation(
            summary = "Agregar producto al carrito",
            description = """
                    Agrega un producto al carrito del usuario. Si el carrito no existe, se crea automáticamente.
                    Si el producto ya existe en el carrito, se actualiza la cantidad.
                    
                    **Validaciones:**
                    - Stock disponible en inventario
                    - Cantidad mayor a 0
                    - Producto existente en catálogo
                    """,
            parameters = {
                    @Parameter(
                            name = "X-Auth-User-Id",
                            description = "ID del usuario autenticado",
                            required = true,
                            example = "123"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Producto agregado exitosamente al carrito",
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
                                                  "descripcion": "Laptop profesional",
                                                  "cantidad": 1,
                                                  "precioUnitario": 1500.00,
                                                  "precioTotal": 1500
                                                },
                                                {
                                                  "id": 2,
                                                  "productoId": 200,
                                                  "nombre": "Mouse Logitech",
                                                  "descripcion": "Mouse inalámbrico",
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
                    responseCode = "400",
                    description = "Stock insuficiente o cantidad inválida",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 400,
                                              "message": "Stock insuficiente para el producto con ID 100. Stock disponible: 5, solicitado: 10"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado en inventario",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Producto con ID 999 no encontrado en inventario"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/agregarproducto")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CartWithProductsDto> addProductToCart(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del producto a agregar",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AddProductRequestDTO.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "productId": 100,
                                              "quantity": 2
                                            }
                                            """
                            )
                    )
            )
            AddProductRequestDTO requestDTO) {
        return cartService.addProductToCart(userId, requestDTO.getProductId(), requestDTO.getQuantity());
    }


    @Operation(
            summary = "Eliminar producto del carrito",
            description = """
                    Elimina un producto específico del carrito del usuario.
                    Actualiza automáticamente el contador de productos y la fecha de último movimiento.
                    """,
            parameters = {
                    @Parameter(
                            name = "X-Auth-User-Id",
                            description = "ID del usuario autenticado",
                            required = true,
                            example = "123"
                    ),
                    @Parameter(
                            name = "productId",
                            description = "ID del producto a eliminar del carrito",
                            required = true,
                            example = "100"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto eliminado exitosamente del carrito",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CartWithProductsDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Carrito o producto no encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Producto con ID 100 no encontrado en el carrito"
                                            }
                                            """
                            )
                    )
            )
    })
    @DeleteMapping("/eliminarproducto/{productId}")
    public Mono<CartWithProductsDto> removeProductFromCart(
            @RequestHeader("X-Auth-User-Id") Long userId,
            @PathVariable Long productId) {
        return cartService.removeProductFromCart(userId, productId);
    }


    @Operation(
            summary = "Ver carrito del usuario",
            description = """
                    Retorna el carrito completo del usuario con todos los productos y sus detalles.
                    Incluye información actualizada de inventario (nombre, descripción, precio).
                    """,
            parameters = {
                    @Parameter(
                            name = "X-Auth-User-Id",
                            description = "ID del usuario autenticado",
                            required = true,
                            example = "123"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Carrito obtenido exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CartWithProductsDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Carrito no encontrado para el usuario",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 404,
                                              "message": "Carrito no encontrado para el usuario con ID 123"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/vercarrito")
    public Mono<CartWithProductsDto> viewCart(@RequestHeader("X-Auth-User-Id") Long userId) {
        return cartService.viewCart(userId);
    }

    @Operation(
            summary = "Vaciar carrito completo",
            description = """
                    Elimina todos los productos del carrito del usuario.
                    El carrito se mantiene activo pero sin productos.
                    """,
            parameters = {
                    @Parameter(
                            name = "X-Auth-User-Id",
                            description = "ID del usuario autenticado",
                            required = true,
                            example = "123"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Carrito vaciado exitosamente",
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
                                              "numeroProductos": 0,
                                              "totalUnidades": 0,
                                              "precioTotal": 0,
                                              "createdAt": "2025-11-15T10:30:00",
                                              "ultimoMovimiento": "2025-11-15T14:20:00",
                                              "products": []
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Carrito no encontrado",
                    content = @Content(mediaType = "application/json")
            )
    })
    @DeleteMapping("/vaciarcarrito")
    public Mono<CartWithProductsDto> clearCart(@RequestHeader("X-Auth-User-Id") Long userId) {
        return cartService.clearCart(userId);
    }

    @Operation(
            summary = "Realizar compra / Checkout",
            description = """
                    Crea una nueva orden a partir del carrito actual del usuario.
                    
                    **Proceso:**
                    1. Valida que el carrito no esté vacío
                    2. Crea la orden en el microservicio de órdenes
                    3. Retorna el carrito antes de eliminarlo
                    4. Elimina el carrito del usuario
                    
                    **Validaciones:**
                    - El carrito debe existir
                    - El carrito debe tener al menos 1 producto
                    """,
            parameters = {
                    @Parameter(
                            name = "X-Auth-User-Id",
                            description = "ID del usuario autenticado",
                            required = true,
                            example = "123"
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Compra realizada exitosamente, orden creada",
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
                                              "totalUnidades": 3,
                                              "precioTotal": 1650,
                                              "createdAt": "2025-11-15T10:30:00",
                                              "ultimoMovimiento": "2025-11-15T15:00:00",
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
                                                  "productoId": 150,
                                                  "nombre": "Teclado Mecánico",
                                                  "cantidad": 2,
                                                  "precioUnitario": 75.00,
                                                  "precioTotal": 150
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Carrito vacío, no se puede crear orden",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "status": 400,
                                              "message": "El carrito del usuario con ID 123 está vacío. No se puede crear una orden."
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
                                              "message": "Carrito no encontrado para el usuario con ID 123"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/realizarcompra")
    public Mono<CartWithProductsDto> checkoutCart(@RequestHeader("X-Auth-User-Id") Long userId) {
        return newOrdenService.crearNuevaOrden(userId);
    }

}
