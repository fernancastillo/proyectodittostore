package com.dittostore.businessdomain.pedidosservice.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoConfirmadoEvent implements Serializable {

    private Long pedidoId;
    private Long usuarioId;
    private BigDecimal total;
    private LocalDateTime fechaConfirmacion;
}