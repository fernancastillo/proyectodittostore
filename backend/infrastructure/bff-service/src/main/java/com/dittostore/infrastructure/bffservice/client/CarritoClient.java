package com.dittostore.infrastructure.bffservice.client;

import com.dittostore.infrastructure.bffservice.dto.CarritoDTO;
import com.dittostore.infrastructure.bffservice.dto.CarritoItemServiceRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "carrito-service")
public interface CarritoClient {

    @GetMapping("/api/carritos/usuario/{usuarioId}/activo")
    CarritoDTO obtenerOCrearActivo(@PathVariable Long usuarioId);

    @PostMapping("/api/carritos/{id}/items")
    CarritoDTO agregarItem(@PathVariable Long id, @RequestBody CarritoItemServiceRequestDTO itemDTO);

    @PatchMapping("/api/carritos/{id}/items/{itemId}/incrementar")
    CarritoDTO incrementarItem(@PathVariable Long id, @PathVariable Long itemId);

    @PatchMapping("/api/carritos/{id}/items/{itemId}/decrementar")
    CarritoDTO decrementarItem(@PathVariable Long id, @PathVariable Long itemId);

    @DeleteMapping("/api/carritos/{id}/items/{itemId}")
    CarritoDTO eliminarItem(@PathVariable Long id, @PathVariable Long itemId);

    @DeleteMapping("/api/carritos/{id}/items")
    CarritoDTO vaciarItems(@PathVariable Long id);
}