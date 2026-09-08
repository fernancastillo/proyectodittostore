package com.dittostore.businessdomain.usuariosservice.dto;

import com.dittostore.businessdomain.usuariosservice.entity.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {

    @NotBlank(message = "El azureAdObjectId es obligatorio")
    private String azureAdObjectId;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato valido")
    private String email;

    private String telefono;

    private String direccion;

    private RolUsuario rol;
}