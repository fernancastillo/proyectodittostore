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
public class ExchangeRequestDTO {

    @NotBlank(message = "El nombre del exchange es obligatorio")
    @Pattern(regexp = ValidacionRabbit.NOMBRE_REGEX, message = ValidacionRabbit.NOMBRE_MENSAJE)
    private String nombre;

    @NotBlank(message = "El tipo de exchange es obligatorio")
    @Pattern(regexp = "direct|topic|fanout|headers", message = "Tipo de exchange inválido (direct, topic, fanout o headers)")
    @Builder.Default
    private String tipo = "direct";

    private Boolean durable;

    private Boolean autoDelete;

    public boolean isDurable() {
        return durable == null || durable;
    }

    public boolean isAutoDelete() {
        return autoDelete != null && autoDelete;
    }
}