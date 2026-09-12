package com.dittostore.infrastructure.bffservice.exception;

public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException(String nombreProducto, Integer stockDisponible, Integer cantidadSolicitada) {
        super("Stock insuficiente para '" + nombreProducto + "'. Disponible: " + stockDisponible
                + ", solicitado: " + cantidadSolicitada);
    }
}