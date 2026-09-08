package com.dittostore.businessdomain.pagoservice.controller;

import com.dittostore.businessdomain.pagoservice.dto.EstadoPagoUpdateRequestDTO;
import com.dittostore.businessdomain.pagoservice.dto.PagoRequestDTO;
import com.dittostore.businessdomain.pagoservice.dto.PagoResponseDTO;
import com.dittostore.businessdomain.pagoservice.service.PagoService;
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
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    public ResponseEntity<PagoResponseDTO> crear(@Valid @RequestBody PagoRequestDTO requestDTO) {
        PagoResponseDTO creado = pagoService.crear(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.obtenerPorId(id));
    }

    // Endpoint que consume el BFF a través de PagoClient (FeignClient)
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<PagoResponseDTO>> obtenerPorPedido(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(pagoService.obtenerPorPedido(pedidoId));
    }

    @GetMapping
    public ResponseEntity<List<PagoResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(pagoService.obtenerTodos());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PagoResponseDTO> actualizarEstado(@PathVariable Long id,
                                                              @Valid @RequestBody EstadoPagoUpdateRequestDTO body) {
        return ResponseEntity.ok(pagoService.actualizarEstado(id, body.getEstado()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
