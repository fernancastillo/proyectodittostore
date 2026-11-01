package com.dittostore.infrastructure.kafkaadminservice.exception;

public class RecursoExistenteException extends RuntimeException {

    public RecursoExistenteException(String mensaje) {
        super(mensaje);
    }
}