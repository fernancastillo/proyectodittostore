package com.dittostore.businessdomain.pagoservice.dto;

import com.dittostore.businessdomain.pagoservice.entity.EstadoPago;
import com.dittostore.businessdomain.pagoservice.entity.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoResponseDTO {

    private Long id;
    private Long pedidoId;
    private BigDecimal monto;
    private MetodoPago metodoPago;
    private EstadoPago estado;
    private String transaccionId;
    private LocalDateTime fechaPago;
}
