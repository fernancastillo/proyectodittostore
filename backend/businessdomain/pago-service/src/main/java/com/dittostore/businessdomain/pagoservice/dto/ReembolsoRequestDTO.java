package com.dittostore.businessdomain.pagoservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReembolsoRequestDTO {

    @NotBlank(message = "motivo es obligatorio")
    @Size(max = 255, message = "motivo no puede superar 255 caracteres")
    private String motivo;
}