package com.dittostore.infrastructure.bffservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "carrito-service")
public interface CarritoClient {

    @GetMapping("/api/carritos/usuario/{usuarioId}")
    Object obtenerCarritoPorUsuario(@PathVariable Long usuarioId);
}