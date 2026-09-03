package com.dittostore.businessdomain.carritoservice.service;

import com.dittostore.businessdomain.carritoservice.dto.CarritoItemRequestDTO;
import com.dittostore.businessdomain.carritoservice.dto.CarritoRequestDTO;
import com.dittostore.businessdomain.carritoservice.dto.CarritoResponseDTO;
import com.dittostore.businessdomain.carritoservice.entity.Carrito;
import com.dittostore.businessdomain.carritoservice.entity.CarritoItem;
import com.dittostore.businessdomain.carritoservice.entity.EstadoCarrito;
import com.dittostore.businessdomain.carritoservice.exception.CarritoNotFoundException;
import com.dittostore.businessdomain.carritoservice.repository.CarritoItemRepository;
import com.dittostore.businessdomain.carritoservice.repository.CarritoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private CarritoItemRepository carritoItemRepository;

    private CarritoServiceImpl carritoService;

    private Carrito carrito;

    @BeforeEach
    void setUp() {
        carritoService = new CarritoServiceImpl(carritoRepository, carritoItemRepository);

        carrito = Carrito.builder()
                .id(1L)
                .usuarioId(10L)
                .estado(EstadoCarrito.ACTIVO)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    @Test
    void crear_deberiaGuardarCarritoActivo() {
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);
        when(carritoItemRepository.findByCarritoId(1L)).thenReturn(List.of());

        CarritoRequestDTO requestDTO = new CarritoRequestDTO();
        requestDTO.setUsuarioId(10L);

        CarritoResponseDTO resultado = carritoService.crear(requestDTO);

        assertEquals(EstadoCarrito.ACTIVO, resultado.getEstado());
        verify(carritoRepository, times(1)).save(any(Carrito.class));
    }

    @Test
    void obtenerPorId_inexistente_deberiaLanzarExcepcion() {
        when(carritoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CarritoNotFoundException.class,
                () -> carritoService.obtenerPorId(99L));
    }

    @Test
    void agregarItem_deberiaGuardarItemEnCarritoExistente() {
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(carritoItemRepository.findByCarritoId(1L)).thenReturn(List.of());

        CarritoItemRequestDTO itemDTO = new CarritoItemRequestDTO();
        itemDTO.setProductoId(5L);
        itemDTO.setCantidad(2);
        itemDTO.setPrecioUnitario(new BigDecimal("55000"));

        carritoService.agregarItem(1L, itemDTO);

        verify(carritoItemRepository, times(1)).save(any(CarritoItem.class));
    }

    @Test
    void eliminar_inexistente_deberiaLanzarExcepcion() {
        when(carritoRepository.existsById(99L)).thenReturn(false);

        assertThrows(CarritoNotFoundException.class,
                () -> carritoService.eliminar(99L));
    }
}