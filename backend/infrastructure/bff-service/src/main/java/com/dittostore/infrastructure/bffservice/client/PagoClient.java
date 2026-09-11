package com.dittostore.infrastructure.bffservice.client;

import com.dittostore.infrastructure.bffservice.dto.EstadoPagoUpdateRequestDTO;
import com.dittostore.infrastructure.bffservice.dto.PagoRequestDTO;
import com.dittostore.infrastructure.bffservice.dto.PagoResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "pago-service")
public interface PagoClient {

    @GetMapping("/api/pagos/pedido/{pedidoId}")
    Object obtenerPagoPorPedido(@PathVariable Long pedidoId);

    @PostMapping("/api/pagos")
    PagoResponseDTO crearPago(@RequestBody PagoRequestDTO request);

    @PatchMapping("/api/pagos/{id}/estado")
    PagoResponseDTO actualizarEstadoPago(@PathVariable Long id, @RequestBody EstadoPagoUpdateRequestDTO request);
}