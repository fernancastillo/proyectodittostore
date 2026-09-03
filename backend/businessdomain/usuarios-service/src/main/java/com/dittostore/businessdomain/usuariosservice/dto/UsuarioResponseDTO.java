package com.dittostore.businessdomain.usuariosservice.dto;

import com.dittostore.businessdomain.usuariosservice.entity.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {

    private Long id;
    private String azureAdObjectId;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String direccion;
    private RolUsuario rol;
}