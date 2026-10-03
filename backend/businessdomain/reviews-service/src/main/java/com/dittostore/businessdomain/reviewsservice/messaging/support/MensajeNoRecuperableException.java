package com.dittostore.businessdomain.reviewsservice.messaging.support;

public class MensajeNoRecuperableException extends RuntimeException {

    public MensajeNoRecuperableException(String mensaje) {
        super(mensaje);
    }
}