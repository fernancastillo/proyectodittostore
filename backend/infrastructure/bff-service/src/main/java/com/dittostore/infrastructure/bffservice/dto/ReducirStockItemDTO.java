package com.dittostore.infrastructure.bffservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReducirStockItemDTO {
    private Long productoId;
    private Integer cantidad;
}