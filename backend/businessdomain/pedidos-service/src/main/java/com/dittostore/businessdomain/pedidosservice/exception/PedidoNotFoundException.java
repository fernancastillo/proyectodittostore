package com.dittostore.businessdomain.pedidosservice.exception;

public class PedidoNotFoundException extends RuntimeException {

    public PedidoNotFoundException(Long id) {
        super("No se encontró el pedido con id " + id);
    }
}
