package com.dittostore.businessdomain.pedidosservice.controller;

import com.dittostore.businessdomain.pedidosservice.dto.AlertaPedidoResponseDTO;
import com.dittostore.businessdomain.pedidosservice.service.AlertaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    public ResponseEntity<List<AlertaPedidoResponseDTO>> listar() {
        return ResponseEntity.ok(alertaService.listar());
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<AlertaPedidoResponseDTO>> listarPorPedido(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(alertaService.listarPorPedido(pedidoId));
    }
}