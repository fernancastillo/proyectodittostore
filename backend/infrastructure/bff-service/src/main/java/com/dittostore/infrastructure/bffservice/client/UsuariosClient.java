package com.dittostore.infrastructure.bffservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "usuarios-service")
public interface UsuariosClient {

    @GetMapping("/api/usuarios/{azureAdObjectId}")
    Object obtenerUsuarioPorAzureId(@PathVariable String azureAdObjectId);
}