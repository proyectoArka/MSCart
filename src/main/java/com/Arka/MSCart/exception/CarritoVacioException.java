package com.Arka.MSCart.exception;

// Excepción personalizada para carritos vacíos
public class CarritoVacioException extends RuntimeException {

    private static final String CARRITO_VACIO_MENSAJE = "El carrito está vacío. No se puede crear una orden.";
    private static final String CARRITO_VACIO_CON_ID = "El carrito del usuario con ID %d está vacío. No se puede crear una orden.";

    public CarritoVacioException(String message) {
        super(message);
    }

    // Constructor por defecto
    public static CarritoVacioException conMensajeDefault() {
        return new CarritoVacioException(CARRITO_VACIO_MENSAJE);
    }

    // Constructor estático para crear la excepción con el ID del usuario
    public static CarritoVacioException paraUsuario(Long userId) {
        return new CarritoVacioException(
            String.format(CARRITO_VACIO_CON_ID, userId)
        );
    }
}
