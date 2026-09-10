package com.dittostore.infrastructure.bffservice.controller;

import com.dittostore.infrastructure.bffservice.client.PedidosClient;
import com.dittostore.infrastructure.bffservice.client.ProductoClient;
import com.dittostore.infrastructure.bffservice.client.UsuariosClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/pedidos")
    public Object obtenerPedidos() {
        return pedidosClient.obtenerTodos();
    }

    @GetMapping("/usuarios")
    public Object obtenerUsuarios() {
        return usuariosClient.obtenerTodos();
    }
}