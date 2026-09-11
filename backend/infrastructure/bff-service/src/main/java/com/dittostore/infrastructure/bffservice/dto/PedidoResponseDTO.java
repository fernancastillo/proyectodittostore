package com.dittostore.infrastructure.bffservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO {
    private Long id;
    private Long usuarioId;
    private LocalDateTime fechaPedido;
    private String estado;
    private String direccionEnvio;
    private BigDecimal total;
    private List<PedidoItemResponseDTO> items;
}