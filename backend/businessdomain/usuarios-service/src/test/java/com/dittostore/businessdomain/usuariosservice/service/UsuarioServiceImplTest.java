package com.dittostore.businessdomain.usuariosservice.service;

import com.dittostore.businessdomain.usuariosservice.dto.UsuarioRequestDTO;
import com.dittostore.businessdomain.usuariosservice.dto.UsuarioResponseDTO;
import com.dittostore.businessdomain.usuariosservice.entity.RolUsuario;
import com.dittostore.businessdomain.usuariosservice.entity.Usuario;
import com.dittostore.businessdomain.usuariosservice.exception.UsuarioNotFoundException;
import com.dittostore.businessdomain.usuariosservice.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioExistente;
    private UsuarioRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        usuarioExistente = Usuario.builder()
            .id(1L)
            .azureAdObjectId("oid-123")
            .nombre("Ash")
            .apellido("Ketchum")
            .email("ash.ketchum@test.com")
            .telefono("+56911111111")
            .direccion("Pueblo Paleta 123")
            .rol(RolUsuario.CLIENTE)
            .build();

        requestDTO = new UsuarioRequestDTO();
        requestDTO.setAzureAdObjectId("oid-123");
        requestDTO.setNombre("Ash");
        requestDTO.setApellido("Ketchum");
        requestDTO.setEmail("ash.ketchum@test.com");
        requestDTO.setRol(RolUsuario.CLIENTE);
    }

    @Test
    void crear_deberiaGuardarYRetornarUsuario() {
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioExistente);

        UsuarioResponseDTO resultado = usuarioService.crear(requestDTO);

        assertNotNull(resultado);
        assertEquals("oid-123", resultado.getAzureAdObjectId());
        assertEquals("Ash", resultado.getNombre());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void obtenerPorAzureAdObjectId_cuandoExiste_deberiaRetornarUsuario() {
        when(usuarioRepository.findByAzureAdObjectId("oid-123"))
            .thenReturn(Optional.of(usuarioExistente));

        UsuarioResponseDTO resultado = usuarioService.obtenerPorAzureAdObjectId("oid-123");

        assertEquals("Ash", resultado.getNombre());
        verify(usuarioRepository, times(1)).findByAzureAdObjectId("oid-123");
    }

    @Test
    void obtenerPorAzureAdObjectId_cuandoNoExiste_deberiaLanzarExcepcion() {
        when(usuarioRepository.findByAzureAdObjectId("oid-inexistente"))
            .thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class,
            () -> usuarioService.obtenerPorAzureAdObjectId("oid-inexistente"));
    }

    @Test
    void obtenerOCrearPorAzureId_cuandoYaExiste_noDeberiaCrearUno_nuevo() {
        when(usuarioRepository.findByAzureAdObjectId("oid-123"))
            .thenReturn(Optional.of(usuarioExistente));

        UsuarioResponseDTO resultado = usuarioService.obtenerOCrearPorAzureId(requestDTO);

        assertEquals("Ash", resultado.getNombre());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void obtenerOCrearPorAzureId_cuandoNoExiste_deberiaCrearUno_nuevo() {
        when(usuarioRepository.findByAzureAdObjectId("oid-nuevo"))
            .thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioExistente);

        UsuarioRequestDTO nuevoDto = new UsuarioRequestDTO();
        nuevoDto.setAzureAdObjectId("oid-nuevo");
        nuevoDto.setNombre("Ash");
        nuevoDto.setApellido("Ketchum");
        nuevoDto.setEmail("ash.ketchum@test.com");

        UsuarioResponseDTO resultado = usuarioService.obtenerOCrearPorAzureId(nuevoDto);

        assertNotNull(resultado);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void eliminar_cuandoNoExiste_deberiaLanzarExcepcion() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        assertThrows(UsuarioNotFoundException.class, () -> usuarioService.eliminar(99L));
        verify(usuarioRepository, never()).deleteById(any());
    }
}