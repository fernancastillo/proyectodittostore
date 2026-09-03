package com.dittostore.infrastructure.bffservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "pago-service")
public interface PagoClient {

    @GetMapping("/api/pagos/pedido/{pedidoId}")
    Object obtenerPagoPorPedido(@PathVariable Long pedidoId);
}