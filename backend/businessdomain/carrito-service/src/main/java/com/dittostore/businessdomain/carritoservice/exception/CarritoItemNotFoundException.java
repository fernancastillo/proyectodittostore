package com.dittostore.businessdomain.carritoservice.exception;

public class CarritoItemNotFoundException extends RuntimeException {
    public CarritoItemNotFoundException(Long id) {
        super("Item de carrito no encontrado con id: " + id);
    }
}