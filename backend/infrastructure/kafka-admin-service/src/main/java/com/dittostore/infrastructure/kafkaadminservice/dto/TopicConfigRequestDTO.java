package com.dittostore.infrastructure.kafkaadminservice.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record TopicConfigRequestDTO(
        @NotEmpty(message = "Debe indicar al menos una configuración")
        Map<String, String> configuraciones) {
}