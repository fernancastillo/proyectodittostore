package com.dittostore.businessdomain.reviewsservice.messaging.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Evento que publica pago-service cuando un pago queda APROBADO.
 * Acepta tambien el JSON completo de PagoResponseDTO (id -> pagoId, campos extra ignorados).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagoAprobadoEvent {

    @JsonAlias("id")
    private Long pagoId;

    private Long pedidoId;

    private BigDecimal monto;

    private String estado;

    private String transaccionId;
}
