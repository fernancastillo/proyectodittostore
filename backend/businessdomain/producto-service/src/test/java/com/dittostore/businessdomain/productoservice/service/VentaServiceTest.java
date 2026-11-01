package com.dittostore.businessdomain.productoservice.service;

import com.dittostore.businessdomain.productoservice.dto.VentaProductoResponseDTO;
import com.dittostore.businessdomain.productoservice.entity.PedidoVentaProcesado;
import com.dittostore.businessdomain.productoservice.entity.Producto;
import com.dittostore.businessdomain.productoservice.entity.ProductoVenta;
import com.dittostore.businessdomain.productoservice.messaging.kafka.event.PedidoEventoKafka;
import com.dittostore.businessdomain.productoservice.repository.PedidoVentaProcesadoRepository;
import com.dittostore.businessdomain.productoservice.repository.ProductoRepository;
import com.dittostore.businessdomain.productoservice.repository.ProductoVentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private ProductoVentaRepository ventaRepository;

    @Mock
    private PedidoVentaProcesadoRepository procesadoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private PedidoEventoKafka evento() {
        return new PedidoEventoKafka(7L, 1L, "CONFIRMADO", new BigDecimal("30000"),
                List.of(new PedidoEventoKafka.Item(1L, 2, new BigDecimal("30000"))), LocalDateTime.now());
    }

    @Test
    void registrarVentaPedido_siElPedidoYaFueContabilizado_noSumaNada() {
        when(procesadoRepository.existsById(7L)).thenReturn(true);

        assertThat(ventaService.registrarVentaPedido(evento())).isFalse();

        verifyNoInteractions(ventaRepository);
        verify(procesadoRepository, never()).save(any(PedidoVentaProcesado.class));
    }

    @Test
    void registrarVentaPedido_conProductoNuevo_creaElRegistroConLasUnidades() {
        when(procesadoRepository.existsById(7L)).thenReturn(false);
        when(ventaRepository.buscarParaActualizar(1L)).thenReturn(Optional.empty());

        assertThat(ventaService.registrarVentaPedido(evento())).isTrue();

        ArgumentCaptor<ProductoVenta> captor = ArgumentCaptor.forClass(ProductoVenta.class);
        verify(ventaRepository).save(captor.capture());
        assertThat(captor.getValue().getProductoId()).isEqualTo(1L);
        assertThat(captor.getValue().getUnidadesVendidas()).isEqualTo(2L);
        assertThat(captor.getValue().getMontoTotal()).isEqualByComparingTo("30000");
        verify(procesadoRepository).save(any(PedidoVentaProcesado.class));
    }

    @Test
    void registrarVentaPedido_conProductoExistente_acumulaUnidadesYMonto() {
        ProductoVenta existente = ProductoVenta.builder()
                .productoId(1L)
                .unidadesVendidas(5L)
                .montoTotal(new BigDecimal("100000"))
                .ultimaActualizacion(LocalDateTime.now())
                .build();
        when(procesadoRepository.existsById(7L)).thenReturn(false);
        when(ventaRepository.buscarParaActualizar(1L)).thenReturn(Optional.of(existente));

        assertThat(ventaService.registrarVentaPedido(evento())).isTrue();

        assertThat(existente.getUnidadesVendidas()).isEqualTo(7L);
        assertThat(existente.getMontoTotal()).isEqualByComparingTo("130000");
        verify(ventaRepository).save(existente);
    }

    @Test
    void obtenerVentas_incluyeElNombreDelProductoOUnTextoSiNoExiste() {
        when(productoRepository.findAll())
                .thenReturn(List.of(Producto.builder().id(1L).nombre("Caja Evoluciones Prismáticas").build()));
        when(ventaRepository.findAllByOrderByUnidadesVendidasDesc()).thenReturn(List.of(
                ProductoVenta.builder().productoId(1L).unidadesVendidas(9L)
                        .montoTotal(new BigDecimal("90000")).ultimaActualizacion(LocalDateTime.now()).build(),
                ProductoVenta.builder().productoId(99L).unidadesVendidas(1L)
                        .montoTotal(new BigDecimal("1000")).ultimaActualizacion(LocalDateTime.now()).build()));

        List<VentaProductoResponseDTO> resultado = ventaService.obtenerVentas();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).nombre()).isEqualTo("Caja Evoluciones Prismáticas");
        assertThat(resultado.get(0).unidadesVendidas()).isEqualTo(9L);
        assertThat(resultado.get(1).nombre()).isEqualTo("(producto no encontrado)");
    }
}