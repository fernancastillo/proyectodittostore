package com.dittostore.businessdomain.productoservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VentaProductoResponseDTO(
        Long productoId,
        String nombre,
        Long unidadesVendidas,
        BigDecimal montoTotal,
        LocalDateTime ultimaActualizacion) {
}