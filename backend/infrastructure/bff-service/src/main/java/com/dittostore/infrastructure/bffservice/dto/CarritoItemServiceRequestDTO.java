package com.dittostore.infrastructure.bffservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItemServiceRequestDTO {
    private Long productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
}