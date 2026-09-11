package com.dittostore.infrastructure.bffservice.controller;

import com.dittostore.infrastructure.bffservice.client.ProductoClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// Controlador público de productos: este es el que consume el catálogo
// de cliente (ProductoService en Angular -> GET /bff/productos).
// Es distinto del AdminController, que expone /bff/admin/productos
// y está pensado para el panel de administración.
@RestController
@RequestMapping("/bff")
@RequiredArgsConstructor
public class ProductoPublicoController {

    // Reutilizamos el mismo Feign Client que ya usa el AdminController,
    // así no duplicamos la conexión al producto-service.
    private final ProductoClient productoClient;

    // GET http://localhost:8081/bff/productos
    // Devuelve el listado completo de productos para el catálogo.
    @GetMapping("/productos")
    public Object obtenerProductos() {
        return productoClient.obtenerProductos();
    }

    // GET http://localhost:8081/bff/productos/{id}
    // Devuelve el detalle de un producto puntual (para detalle-producto.ts).
    @GetMapping("/productos/{id}")
    public Object obtenerProductoPorId(@PathVariable Long id) {
        return productoClient.obtenerProductoPorId(id);
    }
}