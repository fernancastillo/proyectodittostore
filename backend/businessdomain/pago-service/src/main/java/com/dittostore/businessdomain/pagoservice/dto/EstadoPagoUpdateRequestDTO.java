package com.dittostore.businessdomain.pagoservice.dto;

import com.dittostore.businessdomain.pagoservice.entity.EstadoPago;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstadoPagoUpdateRequestDTO {

    @NotNull(message = "estado es obligatorio")
    private EstadoPago estado;
}
