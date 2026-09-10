package com.dittostore.infrastructure.bffservice.client;

import com.dittostore.infrastructure.bffservice.dto.ProductoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "producto-service")
public interface ProductoClient {

    @GetMapping("/api/productos")
    Object obtenerProductos();

    @GetMapping("/api/productos/{id}")
    Object obtenerProductoPorId(@PathVariable Long id);

    @GetMapping("/api/productos/{id}")
    ProductoDTO obtenerDetalleProducto(@PathVariable Long id);

    @PostMapping("/api/productos")
    Object crearProducto(@RequestBody Object dto);

    @PutMapping("/api/productos/{id}")
    Object actualizarProducto(@PathVariable Long id, @RequestBody Object dto);

    @DeleteMapping("/api/productos/{id}")
    void eliminarProducto(@PathVariable Long id);
}