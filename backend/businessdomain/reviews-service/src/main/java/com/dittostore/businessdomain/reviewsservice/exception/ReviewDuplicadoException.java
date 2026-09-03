package com.dittostore.businessdomain.reviewsservice.exception;

public class ReviewDuplicadoException extends RuntimeException {

    public ReviewDuplicadoException(String mensaje) {
        super(mensaje);
    }
}