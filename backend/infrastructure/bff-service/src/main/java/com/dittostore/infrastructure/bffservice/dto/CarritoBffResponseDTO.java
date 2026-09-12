package com.dittostore.infrastructure.bffservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoBffResponseDTO {
    private Long id;
    private Long usuarioId;
    private String estado;
    private List<CarritoItemBffResponseDTO> items;
}