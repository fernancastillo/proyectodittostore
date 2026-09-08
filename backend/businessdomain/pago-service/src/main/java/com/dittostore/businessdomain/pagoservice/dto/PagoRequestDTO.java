package com.dittostore.businessdomain.pagoservice.dto;

import com.dittostore.businessdomain.pagoservice.entity.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PagoRequestDTO {

    @NotNull(message = "pedidoId es obligatorio")
    private Long pedidoId;

    @NotNull(message = "monto es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotNull(message = "metodoPago es obligatorio")
    private MetodoPago metodoPago;
}
