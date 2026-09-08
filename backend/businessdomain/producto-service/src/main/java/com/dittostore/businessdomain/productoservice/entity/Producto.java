package com.dittostore.businessdomain.productoservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(name = "coleccion_set")
    private String coleccionSet;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_caja", nullable = false)
    private TipoCaja tipoCaja;

    @Column(name = "cantidad_sobres")
    private Integer cantidadSobres;

    @Column(name = "cartas_por_sobre")
    private Integer cartasPorSobre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Idioma idioma;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "imagen_url")
    private String imagenUrl;

    public enum TipoCaja {
        BOOSTER_BOX, ELITE_TRAINER_BOX, DISPLAY, FAT_PACK
    }

    public enum Idioma {
        ESPANOL, INGLES, JAPONES
    }
}