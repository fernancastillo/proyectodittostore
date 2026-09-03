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
        UsuarioSyncDTO dto = new UsuarioSyncDTO(
            jwt.getSubject(), // oid del usuario en Azure AD
            jwt.getClaimAsString("given_name"),
            jwt.getClaimAsString("family_name"),
            extraerEmail(jwt)
        );
        return usuariosClient.sincronizarUsuario(dto);
    }

    private String extraerEmail(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email != null) {
            return email;
        }
        var emails = jwt.getClaimAsStringList("emails");
        return (emails != null && !emails.isEmpty()) ? emails.get(0) : null;
    }
}