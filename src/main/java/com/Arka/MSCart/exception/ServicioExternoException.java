package com.Arka.MSCart.exception;

// Excepción personalizada para errores relacionados con servicios externos
public class ServicioExternoException extends RuntimeException {

    private static final String ERROR_SERVICIO_EXTERNO = "Error al comunicarse con el servicio externo: %s";
    private static final String ERROR_SERVICIO_NO_DISPONIBLE = "El servicio %s no está disponible";

    public ServicioExternoException(String message) {
        super(message);
    }

    public ServicioExternoException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor para errores de servicio externo
    public static ServicioExternoException conServicio(String nombreServicio) {
        return new ServicioExternoException(
            String.format(ERROR_SERVICIO_EXTERNO, nombreServicio)
        );
    }

    // Constructor para servicio no disponible
    public static ServicioExternoException noDisponible(String nombreServicio) {
        return new ServicioExternoException(
            String.format(ERROR_SERVICIO_NO_DISPONIBLE, nombreServicio)
        );
    }

    // Constructor para errores de servicio externo con causa
    public static ServicioExternoException conCausa(String nombreServicio, Throwable cause) {
        return new ServicioExternoException(
            String.format(ERROR_SERVICIO_EXTERNO, nombreServicio),
            cause
        );
    }
}