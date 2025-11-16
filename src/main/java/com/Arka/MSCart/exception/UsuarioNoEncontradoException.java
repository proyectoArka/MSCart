package com.Arka.MSCart.exception;
// Excepción personalizada para indicar que un usuario no fue encontrado
public class UsuarioNoEncontradoException extends RuntimeException {

    private static final String USUARIO_NO_ENCONTRADO = "El usuario con ID %d no encontrado en el sistema de autenticación";

    public UsuarioNoEncontradoException(String message) {
        super(message);
    }

    // Constructor estático para crear la excepción con el ID del usuario
    public static UsuarioNoEncontradoException conId(Long userId) {
        return new UsuarioNoEncontradoException(
            String.format(USUARIO_NO_ENCONTRADO, userId)
        );
    }
}
