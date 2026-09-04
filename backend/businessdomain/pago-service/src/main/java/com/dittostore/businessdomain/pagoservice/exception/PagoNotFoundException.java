package com.dittostore.businessdomain.pagoservice.exception;

public class PagoNotFoundException extends RuntimeException {

    public PagoNotFoundException(Long id) {
        super("No se encontró el pago con id " + id);
    }
}
