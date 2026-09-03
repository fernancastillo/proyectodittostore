package com.dittostore.businessdomain.pedidosservice.dto;

import com.dittostore.businessdomain.pedidosservice.entity.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoResponseDTO {

    private Long id;
    private Long usuarioId;
    private LocalDateTime fechaPedido;
    private EstadoPedido estado;
    private String direccionEnvio;
    private BigDecimal total;
    private List<PedidoItemResponseDTO> items;
}
