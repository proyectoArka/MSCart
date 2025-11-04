package com.Arka.MSCart.exception;

public class OutOfStockException  extends RuntimeException {
    public OutOfStockException(String message) {
        super(message);
    }
}
