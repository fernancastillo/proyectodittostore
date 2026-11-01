package com.dittostore.infrastructure.kafkaadminservice.exception;

public class KafkaAdminException extends RuntimeException {

    public KafkaAdminException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}