package com.dittostore.infrastructure.bffservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "producto-service")
public interface ProductoClient {

    @GetMapping("/api/productos")
    Object obtenerProductos();

    @GetMapping("/api/productos/{id}")
    Object obtenerProductoPorId(@PathVariable Long id);
}