package com.dittostore.infrastructure.bffservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioSyncDTO {
    private String azureAdObjectId;
    private String nombre;
    private String apellido;
    private String email;
}