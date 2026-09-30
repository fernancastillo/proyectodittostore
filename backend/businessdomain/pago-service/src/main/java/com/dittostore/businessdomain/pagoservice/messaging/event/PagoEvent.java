package com.dittostore.businessdomain.pagoservice.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagoEvent {

    private Long pagoId;
    private Long pedidoId;
    private BigDecimal monto;
    private String metodoPago;
    private String estado;
    private String transaccionId;
}