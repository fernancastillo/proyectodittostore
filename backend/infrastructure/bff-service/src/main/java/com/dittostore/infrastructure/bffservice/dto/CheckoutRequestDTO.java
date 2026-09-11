package com.dittostore.infrastructure.bffservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequestDTO {

    @NotBlank(message = "direccionEnvio es obligatoria")
    private String direccionEnvio;

    @NotNull(message = "metodoPago es obligatorio")
    private MetodoPagoBff metodoPago;
}