package com.dittostore.infrastructure.rabbitadminservice.controller;

import com.dittostore.infrastructure.rabbitadminservice.dto.BindingRequestDTO;
import com.dittostore.infrastructure.rabbitadminservice.dto.ExchangeRequestDTO;
import com.dittostore.infrastructure.rabbitadminservice.dto.QueueRequestDTO;
import com.dittostore.infrastructure.rabbitadminservice.service.RabbitAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rabbit/admin")
@RequiredArgsConstructor
public class RabbitAdminController {

    private final RabbitAdminService rabbitAdminService;

    @PostMapping("/queues")
    public ResponseEntity<Void> crearCola(@Valid @RequestBody QueueRequestDTO request) {
        rabbitAdminService.crearCola(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/queues/{nombre}")
    public ResponseEntity<Void> eliminarCola(@PathVariable String nombre) {
        rabbitAdminService.eliminarCola(nombre);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/exchanges")
    public ResponseEntity<Void> crearExchange(@Valid @RequestBody ExchangeRequestDTO request) {
        rabbitAdminService.crearExchange(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/exchanges/{nombre}")
    public ResponseEntity<Void> eliminarExchange(@PathVariable String nombre) {
        rabbitAdminService.eliminarExchange(nombre);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bindings")
    public ResponseEntity<Void> crearBinding(@Valid @RequestBody BindingRequestDTO request) {
        rabbitAdminService.crearBinding(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> eliminarBinding(@Valid @RequestBody BindingRequestDTO request) {
        rabbitAdminService.eliminarBinding(request);
        return ResponseEntity.noContent().build();
    }
}