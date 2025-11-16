package com.Arka.MSCart.exception;

// Excepción personalizada para manejo de productos no encontrados
public class ProductoNoEncontradoException extends RuntimeException {

    private static final String PRODUCTO_NO_ENCONTRADO_INVENTARIO = "Producto con ID %d no encontrado en inventario";
    private static final String PRODUCTO_NO_ENCONTRADO_CARRITO = "Producto con ID %d no encontrado en el carrito";

    public ProductoNoEncontradoException(String message) {
        super(message);
    }

    public ProductoNoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor para producto no encontrado en inventario
    public static ProductoNoEncontradoException enInventario(Long productoId) {
        return new ProductoNoEncontradoException(
            String.format(PRODUCTO_NO_ENCONTRADO_INVENTARIO, productoId)
        );
    }

    // Constructor para producto no encontrado en carrito
    public static ProductoNoEncontradoException enCarrito(Long productoId) {
        return new ProductoNoEncontradoException(
            String.format(PRODUCTO_NO_ENCONTRADO_CARRITO, productoId)
        );
    }
}