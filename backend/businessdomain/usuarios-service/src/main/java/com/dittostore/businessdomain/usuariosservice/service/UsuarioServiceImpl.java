package com.dittostore.businessdomain.usuariosservice.service;

import com.dittostore.businessdomain.usuariosservice.dto.UsuarioRequestDTO;
import com.dittostore.businessdomain.usuariosservice.dto.UsuarioResponseDTO;
import com.dittostore.businessdomain.usuariosservice.entity.RolUsuario;
import com.dittostore.businessdomain.usuariosservice.entity.Usuario;
import com.dittostore.businessdomain.usuariosservice.exception.UsuarioNotFoundException;
import com.dittostore.businessdomain.usuariosservice.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public List<UsuarioResponseDTO> obtenerTodos() {
        return usuarioRepository.findAll().stream()
            .map(this::toResponseDTO)
            .toList();
    }

    @Override
    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con id: " + id));
        return toResponseDTO(usuario);
    }

    @Override
    public UsuarioResponseDTO obtenerPorAzureAdObjectId(String azureAdObjectId) {
        Usuario usuario = usuarioRepository.findByAzureAdObjectId(azureAdObjectId)
            .orElseThrow(() -> new UsuarioNotFoundException(
                "Usuario no encontrado con azureAdObjectId: " + azureAdObjectId));
        return toResponseDTO(usuario);
    }

    @Override
    public UsuarioResponseDTO crear(UsuarioRequestDTO dto) {
        Usuario usuario = Usuario.builder()
            .azureAdObjectId(dto.getAzureAdObjectId())
            .nombre(dto.getNombre())
            .apellido(dto.getApellido())
            .email(dto.getEmail())
            .telefono(dto.getTelefono())
            .direccion(dto.getDireccion())
            .rol(dto.getRol() != null ? dto.getRol() : RolUsuario.CLIENTE)
            .build();

        Usuario guardado = usuarioRepository.save(usuario);
        return toResponseDTO(guardado);
    }

    @Override
    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con id: " + id));

        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefono(dto.getTelefono());
        usuario.setDireccion(dto.getDireccion());
        if (dto.getRol() != null) {
            usuario.setRol(dto.getRol());
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return toResponseDTO(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new UsuarioNotFoundException("Usuario no encontrado con id: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    @Override
    public UsuarioResponseDTO obtenerOCrearPorAzureId(UsuarioRequestDTO dto) {
        return usuarioRepository.findByAzureAdObjectId(dto.getAzureAdObjectId())
            .map(this::toResponseDTO)
            .orElseGet(() -> crear(dto));
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
            .id(usuario.getId())
            .azureAdObjectId(usuario.getAzureAdObjectId())
            .nombre(usuario.getNombre())
            .apellido(usuario.getApellido())
            .email(usuario.getEmail())
            .telefono(usuario.getTelefono())
            .direccion(usuario.getDireccion())
            .rol(usuario.getRol())
            .build();
    }
}