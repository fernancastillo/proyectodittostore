package com.dittostore.infrastructure.rabbitadminservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BindingRequestDTO {

    @NotBlank(message = "El nombre de la cola es obligatorio")
    @Pattern(regexp = ValidacionRabbit.NOMBRE_REGEX, message = ValidacionRabbit.NOMBRE_MENSAJE)
    private String cola;

    @NotBlank(message = "El nombre del exchange es obligatorio")
    @Pattern(regexp = ValidacionRabbit.NOMBRE_REGEX, message = ValidacionRabbit.NOMBRE_MENSAJE)
    private String exchange;

    @NotNull(message = "La routing key no puede ser null (use \"\" si no aplica)")
    @Size(max = 255, message = "La routing key admite máximo 255 caracteres")
    @Builder.Default
    private String routingKey = "";
}