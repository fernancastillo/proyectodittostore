package com.dittostore.businessdomain.reviewsservice.exception;

public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(String mensaje) {
        super(mensaje);
    }
}