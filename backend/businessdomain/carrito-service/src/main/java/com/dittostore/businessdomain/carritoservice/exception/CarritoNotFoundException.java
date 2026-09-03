package com.dittostore.businessdomain.carritoservice.exception;

public class CarritoNotFoundException extends RuntimeException {
    public CarritoNotFoundException(Long id) {
        super("Carrito no encontrado con id: " + id);
    }
}