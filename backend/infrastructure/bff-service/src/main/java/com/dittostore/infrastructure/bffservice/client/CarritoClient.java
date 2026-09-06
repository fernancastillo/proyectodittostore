package com.dittostore.infrastructure.bffservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "carrito-service")
public interface CarritoClient {

    @GetMapping("/api/carritos/usuario/{usuarioId}")
    Object obtenerCarritoPorUsuario(@PathVariable Long usuarioId);

    @PostMapping("/api/carritos")
    Object crearCarrito(@RequestBody Object requestDTO);

    @PostMapping("/api/carritos/{id}/items")
    Object agregarItem(@PathVariable Long id, @RequestBody Object itemDTO);

    @DeleteMapping("/api/carritos/{id}/items/{itemId}")
    Object eliminarItem(@PathVariable Long id, @PathVariable Long itemId);
}