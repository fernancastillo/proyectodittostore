package com.dittostore.businessdomain.reviewsservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewsRequestDTO {

    @NotNull(message = "El productoId es obligatorio")
    private Long productoId;

    @NotNull(message = "El usuarioId es obligatorio")
    private Long usuarioId;

    @NotNull(message = "La calificacion es obligatoria")
    @Min(value = 1, message = "La calificacion minima es 1")
    @Max(value = 5, message = "La calificacion maxima es 5")
    private Integer calificacion;

    @Size(max = 1000, message = "El comentario no puede superar los 1000 caracteres")
    private String comentario;
}