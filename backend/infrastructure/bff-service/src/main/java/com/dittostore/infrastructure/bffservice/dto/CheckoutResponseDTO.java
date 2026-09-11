package com.dittostore.infrastructure.bffservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutResponseDTO {
    private Long pedidoId;
    private String estadoPedido;
    private BigDecimal total;
    private Long pagoId;
    private String estadoPago;
    private String transaccionId;
    private String metodoPago;
}