package com.dittostore.businessdomain.pagoservice.messaging.support;

public class MensajeNoRecuperableException extends RuntimeException {

    public MensajeNoRecuperableException(String mensaje) {
        super(mensaje);
    }
}