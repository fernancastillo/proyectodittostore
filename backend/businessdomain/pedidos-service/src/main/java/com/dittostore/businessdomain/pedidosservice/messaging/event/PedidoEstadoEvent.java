package com.dittostore.businessdomain.pedidosservice.messaging.event;

import java.time.LocalDateTime;

public record PedidoEstadoEvent(Long pedidoId, Long usuarioId, String estado, LocalDateTime fechaEvento) {
}