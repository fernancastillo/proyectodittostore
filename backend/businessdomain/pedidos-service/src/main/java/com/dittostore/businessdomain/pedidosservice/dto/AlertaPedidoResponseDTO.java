package com.dittostore.businessdomain.pedidosservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AlertaPedidoResponseDTO(
        Long id,
        Long pedidoId,
        Long pagoId,
        BigDecimal monto,
        String motivo,
        String severidad,
        LocalDateTime fechaAlerta) {
}