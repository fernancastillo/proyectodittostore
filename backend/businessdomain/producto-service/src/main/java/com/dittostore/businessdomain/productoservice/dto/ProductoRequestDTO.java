package com.dittostore.businessdomain.productoservice.dto;

import com.dittostore.businessdomain.productoservice.entity.Producto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequestDTO {

    @NotBlank
    private String nombre;

    private String descripcion;

    private String coleccionSet;

    @NotNull
    private Producto.TipoCaja tipoCaja;

    private Integer cantidadSobres;

    private Integer cartasPorSobre;

    @NotNull
    private Producto.Idioma idioma;

    @NotNull
    @Positive
    private BigDecimal precio;

    @NotNull
    @PositiveOrZero
    private Integer stock;

    private String imagenUrl;
}