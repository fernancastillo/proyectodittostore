package com.dittostore.infrastructure.rabbitadminservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueRequestDTO {

    @NotBlank(message = "El nombre de la cola es obligatorio")
    @Pattern(regexp = ValidacionRabbit.NOMBRE_REGEX, message = ValidacionRabbit.NOMBRE_MENSAJE)
    private String nombre;

    private Boolean durable;

    private Boolean autoDelete;

    @Pattern(regexp = ValidacionRabbit.NOMBRE_REGEX, message = ValidacionRabbit.NOMBRE_MENSAJE)
    private String deadLetterExchange;

    public boolean isDurable() {
        return durable == null || durable;
    }

    public boolean isAutoDelete() {
        return autoDelete != null && autoDelete;
    }
}