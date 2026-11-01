package com.dittostore.businessdomain.productoservice.messaging.kafka.support;

public class MensajeNoRecuperableException extends RuntimeException {

    public MensajeNoRecuperableException(String mensaje) {
        super(mensaje);
    }
}