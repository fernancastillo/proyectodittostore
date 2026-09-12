package com.dittostore.infrastructure.bffservice.client;

import com.dittostore.infrastructure.bffservice.dto.EstadoUpdateRequestDTO;
import com.dittostore.infrastructure.bffservice.dto.PedidoRequestDTO;
import com.dittostore.infrastructure.bffservice.dto.PedidoResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "pedidos-service")
public interface PedidosClient {

    @GetMapping("/api/pedidos")
    Object obtenerTodos();

    @GetMapping("/api/pedidos/usuario/{usuarioId}")
    Object obtenerPedidosPorUsuario(@PathVariable Long usuarioId);

    @PostMapping("/api/pedidos")
    PedidoResponseDTO crearPedido(@RequestBody PedidoRequestDTO request);

    @PatchMapping("/api/pedidos/{id}/estado")
    PedidoResponseDTO actualizarEstadoPedido(@PathVariable Long id, @RequestBody EstadoUpdateRequestDTO request);
}