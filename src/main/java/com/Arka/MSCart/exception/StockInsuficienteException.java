package com.Arka.MSCart.exception;

// Excepción personalizada para manejar situaciones de stock insuficiente
public class StockInsuficienteException extends RuntimeException {

    private static final String STOCK_INSUFICIENTE = "Stock insuficiente para el producto con ID %d. Stock disponible: %d, solicitado: %d";
    private static final String STOCK_NO_DISPONIBLE = "No hay stock disponible para el producto con ID %d";
    private static final String CANTIDAD_INVALIDA = "La cantidad solicitada debe ser mayor a 0. Cantidad recibida: %d";

    public StockInsuficienteException(String message) {
        super(message);
    }

    // Constructor estático para crear una excepción con detalles específicos
    public static StockInsuficienteException conDetalles(Long productoId, Integer stockDisponible, Long cantidadSolicitada) {
        return new StockInsuficienteException(
            String.format(STOCK_INSUFICIENTE, productoId, stockDisponible, cantidadSolicitada)
        );
    }

    // Constructor estático para crear una excepción cuando no hay stock disponible
    public static StockInsuficienteException sinStock(Long productoId) {
        return new StockInsuficienteException(
            String.format(STOCK_NO_DISPONIBLE, productoId)
        );
    }

    // Constructor estático para crear una excepción cuando la cantidad solicitada es inválida
    public static StockInsuficienteException cantidadInvalida(Long cantidad) {
        return new StockInsuficienteException(
            String.format(CANTIDAD_INVALIDA, cantidad)
        );
    }
}

