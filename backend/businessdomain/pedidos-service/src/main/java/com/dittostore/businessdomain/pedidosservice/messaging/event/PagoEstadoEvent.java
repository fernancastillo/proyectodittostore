package com.dittostore.businessdomain.pedidosservice.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoEstadoEvent(
        Long pagoId,
        Long pedidoId,
        BigDecimal monto,
        String metodoPago,
        String estado,
        String transaccionId,
        LocalDateTime fechaEvento) {
}