package com.dittostore.businessdomain.reviewsservice.exception;

public class PedidoNoRegistradoException extends RuntimeException {

    public PedidoNoRegistradoException(Long pedidoId) {
        super("El pedido " + pedidoId + " aun no esta registrado en reviews-service");
    }
}
