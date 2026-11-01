package com.dittostore.infrastructure.kafkaadminservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record TopicRequestDTO(
        @NotBlank(message = "El nombre del tópico es obligatorio")
        @Pattern(regexp = ValidacionKafka.NOMBRE_REGEX, message = ValidacionKafka.NOMBRE_MENSAJE)
        String nombre,

        @NotNull(message = "El número de particiones es obligatorio")
        @Min(value = 1, message = "Debe haber al menos 1 partición")
        @Max(value = 100, message = "Máximo 100 particiones")
        Integer particiones,

        @NotNull(message = "El factor de replicación es obligatorio")
        @Min(value = 1, message = "El factor de replicación mínimo es 1")
        @Max(value = 10, message = "El factor de replicación máximo es 10")
        Integer replicas,

        Map<String, String> configuraciones) {
}