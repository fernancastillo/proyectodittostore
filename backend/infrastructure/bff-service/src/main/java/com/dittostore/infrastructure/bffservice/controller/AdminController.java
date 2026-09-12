package com.dittostore.infrastructure.bffservice.controller;

import com.dittostore.infrastructure.bffservice.client.PedidosClient;
import com.dittostore.infrastructure.bffservice.client.ProductoClient;
import com.dittostore.infrastructure.bffservice.client.UsuariosClient;
import com.dittostore.infrastructure.bffservice.dto.EstadoUpdateRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bff/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductoClient productoClient;
    private final PedidosClient pedidosClient;
    private final UsuariosClient usuariosClient;

    @GetMapping("/productos")
    public Object obtenerProductos() {
        return productoClient.obtenerProductos();
    }

    @PostMapping("/productos")
    public ResponseEntity<Object> crearProducto(@RequestBody Object dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoClient.crearProducto(dto));
    }

    @PutMapping("/productos/{id}")
    public Object actualizarProducto(@PathVariable Long id, @RequestBody Object dto) {
        return productoClient.actualizarProducto(id, dto);
    }

    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoClient.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pedidos")
    public Object obtenerPedidos() {
        return pedidosClient.obtenerTodos();
    }

    @PatchMapping("/pedidos/{id}/estado")
    public Object actualizarEstadoPedido(@PathVariable Long id, @RequestBody EstadoUpdateRequestDTO body) {
        return pedidosClient.actualizarEstadoPedido(id, body);
    }

    @GetMapping("/usuarios")
    public Object obtenerUsuarios() {
        return usuariosClient.obtenerTodos();
    }

    @PutMapping("/usuarios/{id}")
    public Object actualizarUsuario(@PathVariable Long id, @RequestBody Object dto) {
        return usuariosClient.actualizarUsuario(id, dto);
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuariosClient.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}