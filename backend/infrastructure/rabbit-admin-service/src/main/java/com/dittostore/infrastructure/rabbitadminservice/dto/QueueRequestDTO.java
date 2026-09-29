package com.dittostore.infrastructure.rabbitadminservice.dto;

import jakarta.validation.constraints.NotBlank;
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
    private String nombre;

    private Boolean durable;

    private Boolean exclusive;

    private Boolean autoDelete;

    private String deadLetterExchange;

    public boolean isDurable() {
        return durable == null || durable;
    }

    public boolean isExclusive() {
        return exclusive != null && exclusive;
    }

    public boolean isAutoDelete() {
        return autoDelete != null && autoDelete;
    }
}