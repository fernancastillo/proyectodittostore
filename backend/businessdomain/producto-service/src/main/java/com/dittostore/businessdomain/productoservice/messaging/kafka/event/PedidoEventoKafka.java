package com.dittostore.businessdomain.productoservice.messaging.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoEventoKafka(
        Long pedidoId,
        Long usuarioId,
        String estado,
        BigDecimal total,
        List<Item> items,
        LocalDateTime fechaEvento) {

    public record Item(Long productoId, Integer cantidad, BigDecimal subtotal) {}
}