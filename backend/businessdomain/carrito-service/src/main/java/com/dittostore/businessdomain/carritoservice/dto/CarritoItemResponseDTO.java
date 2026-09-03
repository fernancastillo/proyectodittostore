package com.dittostore.businessdomain.carritoservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoItemResponseDTO {

    private Long id;
    private Long productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
}