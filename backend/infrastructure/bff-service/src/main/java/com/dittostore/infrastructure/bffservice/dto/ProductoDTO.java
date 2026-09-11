package com.dittostore.infrastructure.bffservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String coleccionSet;
    private BigDecimal precio;
    private Integer stock;
    private String imagenUrl;
}