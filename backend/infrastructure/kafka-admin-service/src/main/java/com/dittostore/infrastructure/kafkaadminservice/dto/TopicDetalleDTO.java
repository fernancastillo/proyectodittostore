package com.dittostore.infrastructure.kafkaadminservice.dto;

import java.util.List;
import java.util.Map;

public record TopicDetalleDTO(
        String nombre,
        int particiones,
        int factorReplicacion,
        List<ParticionDTO> detalleParticiones,
        Map<String, String> configuraciones) {
}