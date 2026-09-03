package com.dittostore.businessdomain.productoservice.dto;

import com.dittostore.businessdomain.productoservice.entity.Producto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String coleccionSet;
    private Producto.TipoCaja tipoCaja;
    private Integer cantidadSobres;
    private Integer cartasPorSobre;
    private Producto.Idioma idioma;
    private BigDecimal precio;
    private Integer stock;
    private String imagenUrl;
}