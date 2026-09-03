package com.dittostore.businessdomain.carritoservice.dto;

import com.dittostore.businessdomain.carritoservice.entity.EstadoCarrito;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoResponseDTO {

    private Long id;
    private Long usuarioId;
    private EstadoCarrito estado;
    private LocalDateTime fechaCreacion;
    private List<CarritoItemResponseDTO> items;
}