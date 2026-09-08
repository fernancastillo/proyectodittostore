package com.dittostore.businessdomain.productoservice.service;

import com.dittostore.businessdomain.productoservice.dto.ProductoRequestDTO;
import com.dittostore.businessdomain.productoservice.dto.ProductoResponseDTO;
import com.dittostore.businessdomain.productoservice.entity.Producto;
import com.dittostore.businessdomain.productoservice.exception.ProductoNotFoundException;
import com.dittostore.businessdomain.productoservice.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto producto;
    private ProductoRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        producto = Producto.builder()
                .id(1L)
                .nombre("Caja Escarlata y Púrpura - Evoluciones Prismáticas")
                .coleccionSet("Evoluciones Prismáticas")
                .tipoCaja(Producto.TipoCaja.BOOSTER_BOX)
                .cantidadSobres(36)
                .cartasPorSobre(10)
                .idioma(Producto.Idioma.ESPANOL)
                .precio(new BigDecimal("55000"))
                .stock(20)
                .build();

        requestDTO = new ProductoRequestDTO();
        requestDTO.setNombre(producto.getNombre());
        requestDTO.setColeccionSet(producto.getColeccionSet());
        requestDTO.setTipoCaja(producto.getTipoCaja());
        requestDTO.setCantidadSobres(producto.getCantidadSobres());
        requestDTO.setCartasPorSobre(producto.getCartasPorSobre());
        requestDTO.setIdioma(producto.getIdioma());
        requestDTO.setPrecio(producto.getPrecio());
        requestDTO.setStock(producto.getStock());
    }

    @Test
    void crear_deberiaGuardarYRetornarProducto() {
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        ProductoResponseDTO resultado = productoService.crear(requestDTO);

        assertEquals(producto.getNombre(), resultado.getNombre());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void obtenerPorId_existente_deberiaRetornarProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        ProductoResponseDTO resultado = productoService.obtenerPorId(1L);

        assertEquals(1L, resultado.getId());
    }

    @Test
    void obtenerPorId_inexistente_deberiaLanzarExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductoNotFoundException.class,
                () -> productoService.obtenerPorId(99L));
    }

    @Test
    void eliminar_existente_deberiaEliminar() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        productoService.eliminar(1L);

        verify(productoRepository, times(1)).delete(producto);
    }
}