package com.dittostore.businessdomain.carritoservice.dto;

import com.dittostore.businessdomain.carritoservice.entity.EstadoCarrito;
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
    private EstadoCarrito estado;
}