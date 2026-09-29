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
public class BindingRequestDTO {

    @NotBlank(message = "El nombre de la cola es obligatorio")
    private String cola;

    @NotBlank(message = "El nombre del exchange es obligatorio")
    private String exchange;

    @Builder.Default
    private String routingKey = "";
}