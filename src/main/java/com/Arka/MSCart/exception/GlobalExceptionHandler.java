package com.Arka.MSCart.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

// Manejador global de excepciones para la aplicación
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ========== EXCEPCIONES DE NEGOCIO (400 BAD REQUEST) ==========

    // Maneja carrito vacío
    @ExceptionHandler(CarritoVacioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDetails handleCarritoVacio(CarritoVacioException ex) {
        return new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );
    }

    // Maneja stock insuficiente
    @ExceptionHandler(StockInsuficienteException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDetails handleStockInsuficiente(StockInsuficienteException ex) {
        return new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );
    }

    // Maneja estado ilegal
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDetails handleIllegalState(IllegalStateException ex) {
        return new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );
    }

    // Maneja errores de validación de datos
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

    // ========== EXCEPCIONES DE RECURSO NO ENCONTRADO (404 NOT FOUND) ==========

    // Maneja producto no encontrado
    @ExceptionHandler(ProductoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDetails handleProductoNoEncontrado(ProductoNoEncontradoException ex) {
        return new ErrorDetails(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );
    }

    // Maneja carrito no encontrado
    @ExceptionHandler(CarritoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDetails handleCarritoNoEncontrado(CarritoNoEncontradoException ex) {
        return new ErrorDetails(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );
    }

    // Maneja usuario no encontrado
    @ExceptionHandler(UsuarioNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDetails handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {
        return new ErrorDetails(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );
    }

    // ========== EXCEPCIONES DE SERVICIOS EXTERNOS (503 SERVICE UNAVAILABLE) ==========

    // Maneja errores de servicios externos
    @ExceptionHandler(ServicioExternoException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorDetails handleServicioExterno(ServicioExternoException ex) {
        return new ErrorDetails(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex.getMessage()
        );
    }

    // Clase para representar los detalles de un error
    public record ErrorDetails(int status, String message) {}
}

