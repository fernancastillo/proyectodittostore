package com.dittostore.infrastructure.rabbitadminservice.exception;

public class RabbitAdminException extends RuntimeException {

    public RabbitAdminException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}