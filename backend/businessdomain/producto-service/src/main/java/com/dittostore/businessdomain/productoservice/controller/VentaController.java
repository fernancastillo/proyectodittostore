package com.dittostore.businessdomain.productoservice.controller;

import com.dittostore.businessdomain.productoservice.dto.VentaProductoResponseDTO;
import com.dittostore.businessdomain.productoservice.service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @GetMapping
    public ResponseEntity<List<VentaProductoResponseDTO>> obtenerVentas() {
        return ResponseEntity.ok(ventaService.obtenerVentas());
    }
}