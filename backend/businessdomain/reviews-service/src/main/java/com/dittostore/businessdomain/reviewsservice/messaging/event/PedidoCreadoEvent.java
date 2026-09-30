package com.dittostore.businessdomain.reviewsservice.messaging.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Evento que publica pedidos-service cuando se crea un pedido.
 * Acepta tambien el JSON completo de PedidoResponseDTO (id -> pedidoId, campos extra ignorados).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PedidoCreadoEvent {

    @JsonAlias("id")
    private Long pedidoId;

    private Long usuarioId;

    private List<Item> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private Long productoId;
        private Integer cantidad;
    }
}
