package com.dittostore.businessdomain.pedidosservice.messaging.support;

public class MensajeNoRecuperableException extends RuntimeException {

    public MensajeNoRecuperableException(String mensaje) {
        super(mensaje);
    }
}