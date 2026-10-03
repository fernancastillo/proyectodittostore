package com.dittostore.infrastructure.rabbitadminservice.controller;

import com.dittostore.infrastructure.rabbitadminservice.dto.BindingRequestDTO;
import com.dittostore.infrastructure.rabbitadminservice.dto.ExchangeRequestDTO;
import com.dittostore.infrastructure.rabbitadminservice.dto.QueueRequestDTO;
import com.dittostore.infrastructure.rabbitadminservice.dto.ValidacionRabbit;
import com.dittostore.infrastructure.rabbitadminservice.service.RabbitAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    public ResponseEntity<Void> eliminarCola(
            @PathVariable @Pattern(regexp = ValidacionRabbit.NOMBRE_REGEX, message = ValidacionRabbit.NOMBRE_MENSAJE) String nombre) {
        rabbitAdminService.eliminarCola(nombre);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/exchanges")
    public ResponseEntity<Void> crearExchange(@Valid @RequestBody ExchangeRequestDTO request) {
        rabbitAdminService.crearExchange(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/exchanges/{nombre}")
    public ResponseEntity<Void> eliminarExchange(
            @PathVariable @Pattern(regexp = ValidacionRabbit.NOMBRE_REGEX, message = ValidacionRabbit.NOMBRE_MENSAJE) String nombre) {
        rabbitAdminService.eliminarExchange(nombre);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bindings")
    public ResponseEntity<Void> crearBinding(@Valid @RequestBody BindingRequestDTO request) {
        rabbitAdminService.crearBinding(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> eliminarBinding(
            @RequestParam @Pattern(regexp = ValidacionRabbit.NOMBRE_REGEX, message = ValidacionRabbit.NOMBRE_MENSAJE) String cola,
            @RequestParam @Pattern(regexp = ValidacionRabbit.NOMBRE_REGEX, message = ValidacionRabbit.NOMBRE_MENSAJE) String exchange,
            @RequestParam(defaultValue = "") @Size(max = 255, message = "La routing key admite máximo 255 caracteres") String routingKey) {
        rabbitAdminService.eliminarBinding(BindingRequestDTO.builder()
                .cola(cola).exchange(exchange).routingKey(routingKey).build());
        return ResponseEntity.noContent().build();
    }
}