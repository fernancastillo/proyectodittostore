package com.dittostore.businessdomain.reviewsservice.messaging.event;

import java.time.LocalDateTime;

public record PedidoEstadoEvent(
        Long pedidoId,
        Long usuarioId,
        String estado,
        LocalDateTime fechaEvento) {
}