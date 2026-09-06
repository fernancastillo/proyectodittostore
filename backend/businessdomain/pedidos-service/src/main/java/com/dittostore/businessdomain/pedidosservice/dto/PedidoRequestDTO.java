package com.dittostore.businessdomain.pedidosservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRequestDTO {

    @NotNull(message = "usuarioId es obligatorio")
    private Long usuarioId;

    @NotBlank(message = "direccionEnvio es obligatoria")
    private String direccionEnvio;

    @NotEmpty(message = "el pedido debe tener al menos un item")
    @Valid
    private List<PedidoItemRequestDTO> items;
}
