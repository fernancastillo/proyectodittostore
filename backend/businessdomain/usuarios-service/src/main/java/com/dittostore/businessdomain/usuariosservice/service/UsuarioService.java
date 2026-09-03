package com.dittostore.businessdomain.usuariosservice.service;

import com.dittostore.businessdomain.usuariosservice.dto.UsuarioRequestDTO;
import com.dittostore.businessdomain.usuariosservice.dto.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {

    List<UsuarioResponseDTO> obtenerTodos();

    UsuarioResponseDTO obtenerPorId(Long id);

    UsuarioResponseDTO obtenerPorAzureAdObjectId(String azureAdObjectId);

    UsuarioResponseDTO crear(UsuarioRequestDTO dto);

    UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO dto);

    void eliminar(Long id);

    UsuarioResponseDTO obtenerOCrearPorAzureId(UsuarioRequestDTO dto);
}