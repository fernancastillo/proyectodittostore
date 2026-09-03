package com.dittostore.infrastructure.bffservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "pedidos-service")
public interface PedidosClient {

    @GetMapping("/api/pedidos/usuario/{usuarioId}")
    Object obtenerPedidosPorUsuario(@PathVariable Long usuarioId);
}