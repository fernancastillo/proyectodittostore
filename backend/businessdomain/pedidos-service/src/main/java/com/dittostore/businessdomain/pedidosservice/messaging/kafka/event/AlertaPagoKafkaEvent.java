package com.dittostore.businessdomain.pedidosservice.messaging.kafka.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AlertaPagoKafkaEvent(
        Long pagoId,
        Long pedidoId,
        BigDecimal monto,
        String metodoPago,
        String estado,
        String transaccionId,
        String motivo,
        String severidad,
        LocalDateTime fechaEvento) {
}