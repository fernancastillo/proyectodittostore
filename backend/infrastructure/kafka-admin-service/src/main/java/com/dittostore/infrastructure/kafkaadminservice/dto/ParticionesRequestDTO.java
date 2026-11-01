package com.dittostore.infrastructure.kafkaadminservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ParticionesRequestDTO(
        @NotNull(message = "El nuevo número de particiones es obligatorio")
        @Min(value = 1, message = "Debe haber al menos 1 partición")
        @Max(value = 100, message = "Máximo 100 particiones")
        Integer particiones) {
}