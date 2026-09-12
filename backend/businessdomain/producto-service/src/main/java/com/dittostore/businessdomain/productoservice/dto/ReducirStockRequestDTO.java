package com.dittostore.businessdomain.productoservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReducirStockRequestDTO {

    @NotEmpty(message = "debe incluir al menos un item")
    @Valid
    private List<ReducirStockItemDTO> items;
}