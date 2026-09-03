package com.dittostore.businessdomain.reviewsservice.controller;

import com.dittostore.businessdomain.reviewsservice.dto.ReviewsRequestDTO;
import com.dittostore.businessdomain.reviewsservice.dto.ReviewsResponseDTO;
import com.dittostore.businessdomain.reviewsservice.service.ReviewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewsController {

    private final ReviewsService reviewsService;

    @GetMapping
    public ResponseEntity<List<ReviewsResponseDTO>> obtenerTodas() {
        return ResponseEntity.ok(reviewsService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewsResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reviewsService.obtenerPorId(id));
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<ReviewsResponseDTO>> obtenerPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(reviewsService.obtenerPorProducto(productoId));
    }

    @GetMapping("/producto/{productoId}/promedio")
    public ResponseEntity<Double> obtenerPromedioPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(reviewsService.calcularPromedio(productoId));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ReviewsResponseDTO>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(reviewsService.obtenerPorUsuario(usuarioId));
    }

    @PostMapping
    public ResponseEntity<ReviewsResponseDTO> crear(@Valid @RequestBody ReviewsRequestDTO dto) {
        ReviewsResponseDTO creada = reviewsService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewsResponseDTO> actualizar(
            @PathVariable Long id, @Valid @RequestBody ReviewsRequestDTO dto) {
        return ResponseEntity.ok(reviewsService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reviewsService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}