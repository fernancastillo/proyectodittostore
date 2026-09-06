package com.dittostore.businessdomain.pedidosservice.dto;

import com.dittostore.businessdomain.pedidosservice.entity.EstadoPedido;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstadoUpdateRequestDTO {

    @NotNull(message = "estado es obligatorio")
    private EstadoPedido estado;
}
