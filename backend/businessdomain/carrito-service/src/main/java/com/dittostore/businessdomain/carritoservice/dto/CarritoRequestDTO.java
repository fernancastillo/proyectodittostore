package com.dittostore.businessdomain.carritoservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarritoRequestDTO {

    @NotNull(message = "usuarioId es obligatorio")
    private Long usuarioId;
}