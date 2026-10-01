package com.dittostore.businessdomain.pagoservice.messaging.event;

import com.dittostore.businessdomain.pagoservice.entity.EstadoPago;
import com.dittostore.businessdomain.pagoservice.entity.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoEstadoEvent(
        Long pagoId,
        Long pedidoId,
        BigDecimal monto,
        MetodoPago metodoPago,
        EstadoPago estado,
        String transaccionId,
        LocalDateTime fechaEvento) {
}