package com.dittostore.infrastructure.bffservice.client;

import com.dittostore.infrastructure.bffservice.dto.UsuarioSyncDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "usuarios-service")
public interface UsuariosClient {

    @GetMapping("/api/usuarios/azure/{azureAdObjectId}")
    Object obtenerUsuarioPorAzureId(@PathVariable String azureAdObjectId);

    @PostMapping("/api/usuarios/sync")
    Object sincronizarUsuario(@RequestBody UsuarioSyncDTO dto);
}