package com.dittostore.businessdomain.reviewsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewsResponseDTO {

    private Long id;
    private Long productoId;
    private Long usuarioId;
    private Integer calificacion;
    private String comentario;
}