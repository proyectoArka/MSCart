package com.Arka.MSCart.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CarritoVacioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDetails handleProductoNoEncontrado(CarritoVacioException ex) {
        return new ErrorDetails(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );
    }

    // 1. Maneja Producto No Encontrado (Ej. 404 del Microservicio)
    @ExceptionHandler(ProductoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDetails handleProductoNoEncontrado(ProductoNoEncontradoException ex) {
        return new ErrorDetails(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );
    }

    // 2. Maneja Excepción de Stock Insuficiente (Error de Negocio/Cliente)
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDetails handleIllegalState(IllegalStateException ex) {
        return new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                "Error de Stock: " + ex.getMessage()
        );
    }

    // 3. Maneja Errores de Validación de DTOs (@Valid)
    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDetails handleWebExchangeBindException(WebExchangeBindException ex) {
        String message = ex.getBindingResult().getFieldError() != null ?
                "Error de validación: " + ex.getBindingResult().getFieldError().getDefaultMessage() :
                "Error de validación en la solicitud.";

        return new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                message
        );
    }

    // 4. NUEVO: Maneja errores de servicio externo
    @ExceptionHandler(ServicioExternoException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorDetails handleServicioExterno(ServicioExternoException ex) {
        return new ErrorDetails(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex.getMessage()
        );
    }

    public record ErrorDetails(int status, String message) {}
}