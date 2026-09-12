package com.dittostore.infrastructure.bffservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponseDTO {
    private Long id;
    private Long pedidoId;
    private BigDecimal monto;
    private String metodoPago;
    private String estado;
    private String transaccionId;
    private LocalDateTime fechaPago;
}