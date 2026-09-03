package com.dittostore.businessdomain.carritoservice.controller;

import com.dittostore.businessdomain.carritoservice.dto.CarritoItemRequestDTO;
import com.dittostore.businessdomain.carritoservice.dto.CarritoRequestDTO;
import com.dittostore.businessdomain.carritoservice.dto.CarritoResponseDTO;
import com.dittostore.businessdomain.carritoservice.dto.EstadoUpdateRequestDTO;
import com.dittostore.businessdomain.carritoservice.service.CarritoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/carritos")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @PostMapping
    public ResponseEntity<CarritoResponseDTO> crear(@Valid @RequestBody CarritoRequestDTO requestDTO) {
        CarritoResponseDTO creado = carritoService.crear(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarritoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(carritoService.obtenerPorId(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<CarritoResponseDTO>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(carritoService.obtenerPorUsuario(usuarioId));
    }

    @GetMapping
    public ResponseEntity<List<CarritoResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(carritoService.obtenerTodos());
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<CarritoResponseDTO> agregarItem(@PathVariable Long id,
                                                           @Valid @RequestBody CarritoItemRequestDTO itemDTO) {
        return ResponseEntity.ok(carritoService.agregarItem(id, itemDTO));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<CarritoResponseDTO> eliminarItem(@PathVariable Long id, @PathVariable Long itemId) {
        return ResponseEntity.ok(carritoService.eliminarItem(id, itemId));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<CarritoResponseDTO> actualizarEstado(@PathVariable Long id,
                                                                @Valid @RequestBody EstadoUpdateRequestDTO body) {
        return ResponseEntity.ok(carritoService.actualizarEstado(id, body.getEstado()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        carritoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}