package com.dittostore.infrastructure.kafkaadminservice.controller;

import com.dittostore.infrastructure.kafkaadminservice.dto.GrupoDetalleDTO;
import com.dittostore.infrastructure.kafkaadminservice.dto.ParticionesRequestDTO;
import com.dittostore.infrastructure.kafkaadminservice.dto.TopicConfigRequestDTO;
import com.dittostore.infrastructure.kafkaadminservice.dto.TopicDetalleDTO;
import com.dittostore.infrastructure.kafkaadminservice.dto.TopicRequestDTO;
import com.dittostore.infrastructure.kafkaadminservice.dto.ValidacionKafka;
import com.dittostore.infrastructure.kafkaadminservice.service.KafkaAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kafka/admin")
@RequiredArgsConstructor
public class KafkaAdminController {

    private final KafkaAdminService kafkaAdminService;

    @PostMapping("/topics")
    public ResponseEntity<Void> crearTopic(@Valid @RequestBody TopicRequestDTO request) {
        kafkaAdminService.crearTopic(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/topics")
    public ResponseEntity<List<String>> listarTopics() {
        return ResponseEntity.ok(kafkaAdminService.listarTopics());
    }

    @GetMapping("/topics/{nombre}")
    public ResponseEntity<TopicDetalleDTO> describirTopic(
            @PathVariable @Pattern(regexp = ValidacionKafka.NOMBRE_REGEX, message = ValidacionKafka.NOMBRE_MENSAJE) String nombre) {
        return ResponseEntity.ok(kafkaAdminService.describirTopic(nombre));
    }

    @PutMapping("/topics/{nombre}/config")
    public ResponseEntity<Void> modificarConfiguracion(
            @PathVariable @Pattern(regexp = ValidacionKafka.NOMBRE_REGEX, message = ValidacionKafka.NOMBRE_MENSAJE) String nombre,
            @Valid @RequestBody TopicConfigRequestDTO request) {
        kafkaAdminService.modificarConfiguracion(nombre, request.configuraciones());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/topics/{nombre}/partitions")
    public ResponseEntity<Void> aumentarParticiones(
            @PathVariable @Pattern(regexp = ValidacionKafka.NOMBRE_REGEX, message = ValidacionKafka.NOMBRE_MENSAJE) String nombre,
            @Valid @RequestBody ParticionesRequestDTO request) {
        kafkaAdminService.aumentarParticiones(nombre, request.particiones());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/topics/{nombre}")
    public ResponseEntity<Void> eliminarTopic(
            @PathVariable @Pattern(regexp = ValidacionKafka.NOMBRE_REGEX, message = ValidacionKafka.NOMBRE_MENSAJE) String nombre) {
        kafkaAdminService.eliminarTopic(nombre);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/consumer-groups")
    public ResponseEntity<List<String>> listarGrupos() {
        return ResponseEntity.ok(kafkaAdminService.listarGrupos());
    }

    @GetMapping("/consumer-groups/{groupId}")
    public ResponseEntity<GrupoDetalleDTO> describirGrupo(
            @PathVariable @Pattern(regexp = ValidacionKafka.GRUPO_REGEX, message = ValidacionKafka.GRUPO_MENSAJE) String groupId) {
        return ResponseEntity.ok(kafkaAdminService.describirGrupo(groupId));
    }
}