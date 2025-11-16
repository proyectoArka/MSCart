package com.Arka.MSCart.exception;

// Excepción personalizada para carrito no encontrado
public class CarritoNoEncontradoException extends RuntimeException {

    private static final String CARRITO_NO_ENCONTRADO_USUARIO = "Carrito no encontrado para el usuario con ID %d";
    private static final String CARRITO_NO_ENCONTRADO_ID = "Carrito no encontrado con el ID %d";

    public CarritoNoEncontradoException(String message) {
        super(message);
    }

    // Constructor para carrito no encontrado por ID de usuario
    public static CarritoNoEncontradoException paraUsuario(Long userId) {
        return new CarritoNoEncontradoException(
            String.format(CARRITO_NO_ENCONTRADO_USUARIO, userId)
        );
    }

    // Constructor para carrito no encontrado por ID de carrito
    public static CarritoNoEncontradoException conId(Long cartId) {
        return new CarritoNoEncontradoException(
            String.format(CARRITO_NO_ENCONTRADO_ID, cartId)
        );
    }
}

