package com.dittostore.infrastructure.bffservice.controller;

import com.dittostore.infrastructure.bffservice.client.UsuariosClient;
import com.dittostore.infrastructure.bffservice.dto.UsuarioSyncDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PerfilController {

    private final UsuariosClient usuariosClient;

    @GetMapping("/bff/perfil")
    public Object obtenerPerfil(@AuthenticationPrincipal Jwt jwt) {
        String nombreCompleto = jwt.getClaimAsString("name");
        String[] partes = dividirNombre(nombreCompleto);

        UsuarioSyncDTO dto = new UsuarioSyncDTO(
            jwt.getSubject(),
            partes[0],
            partes[1],
            extraerEmail(jwt)
        );
        return usuariosClient.sincronizarUsuario(dto);
    }

    private String[] dividirNombre(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            return new String[]{"Sin nombre", "Sin apellido"};
        }
        String[] partes = nombreCompleto.trim().split(" ", 2);
        String nombre = partes[0];
        String apellido = partes.length > 1 ? partes[1] : "-";
        return new String[]{nombre, apellido};
    }

    private String extraerEmail(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email != null) {
            return email;
        }
        var emails = jwt.getClaimAsStringList("emails");
        if (emails != null && !emails.isEmpty()) {
            return emails.get(0);
        }
        return jwt.getClaimAsString("preferred_username");
    }
}