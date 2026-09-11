package com.dittostore.infrastructure.bffservice.exception;

public class CarritoVacioException extends RuntimeException {
    public CarritoVacioException() {
        super("No puedes pagar un carrito vacío");
    }
}