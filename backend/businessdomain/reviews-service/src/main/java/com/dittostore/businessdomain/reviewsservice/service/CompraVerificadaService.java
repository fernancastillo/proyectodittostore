package com.dittostore.businessdomain.reviewsservice.service;

public interface CompraVerificadaService {

    /** Idempotente: si (pedidoId, productoId) ya existe, no hace nada. */
    void registrarCompra(Long pedidoId, Long usuarioId, Long productoId, Integer cantidad);

    /** Marca como pagadas todas las compras del pedido. Lanza PedidoNoRegistradoException si no existen. */
    void marcarPagado(Long pedidoId);
}
