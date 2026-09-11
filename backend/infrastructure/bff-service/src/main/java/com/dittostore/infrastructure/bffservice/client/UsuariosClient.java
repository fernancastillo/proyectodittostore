package com.dittostore.infrastructure.bffservice.client;

import com.dittostore.infrastructure.bffservice.dto.UsuarioDTO;
import com.dittostore.infrastructure.bffservice.dto.UsuarioSyncDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "usuarios-service")
public interface UsuariosClient {

    @GetMapping("/api/usuarios")
    Object obtenerTodos();

    @GetMapping("/api/usuarios/azure/{azureAdObjectId}")
    Object obtenerUsuarioPorAzureId(@PathVariable String azureAdObjectId);

    @GetMapping("/api/usuarios/azure/{azureAdObjectId}")
    UsuarioDTO obtenerDetalleUsuarioPorAzureId(@PathVariable String azureAdObjectId);

    @PostMapping("/api/usuarios/sync")
    Object sincronizarUsuario(@RequestBody UsuarioSyncDTO dto);

    @PutMapping("/api/usuarios/{id}")
    Object actualizarUsuario(@PathVariable Long id, @RequestBody Object dto);

    @DeleteMapping("/api/usuarios/{id}")
    void eliminarUsuario(@PathVariable Long id);
}