package com.dittostore.businessdomain.pedidosservice.service;

import com.dittostore.businessdomain.pedidosservice.dto.PedidoItemRequestDTO;
import com.dittostore.businessdomain.pedidosservice.dto.PedidoRequestDTO;
import com.dittostore.businessdomain.pedidosservice.dto.PedidoResponseDTO;
import com.dittostore.businessdomain.pedidosservice.entity.EstadoPedido;
import com.dittostore.businessdomain.pedidosservice.entity.Pedido;
import com.dittostore.businessdomain.pedidosservice.exception.PedidoNotFoundException;
import com.dittostore.businessdomain.pedidosservice.messaging.producer.PedidoProducer;
import com.dittostore.businessdomain.pedidosservice.repository.PedidoItemRepository;
import com.dittostore.businessdomain.pedidosservice.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PedidoItemRepository pedidoItemRepository;

    @Mock
    private PedidoProducer pedidoProducer;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private Pedido pedidoGuardado;

    @BeforeEach
    void setUp() {
        pedidoGuardado = Pedido.builder()
                .id(1L)
                .usuarioId(10L)
                .fechaPedido(LocalDateTime.now())
                .estado(EstadoPedido.PENDIENTE)
                .direccionEnvio("Av. Siempre Viva 123")
                .total(BigDecimal.ZERO)
                .build();
    }

    @Test
    void crear_calculaElTotalSumandoLosSubtotalesDeCadaItem() {
        PedidoItemRequestDTO item1 = new PedidoItemRequestDTO(1L, 2, BigDecimal.valueOf(15000));
        PedidoItemRequestDTO item2 = new PedidoItemRequestDTO(2L, 1, BigDecimal.valueOf(25000));
        PedidoRequestDTO request = new PedidoRequestDTO(10L, "Av. Siempre Viva 123", List.of(item1, item2));

        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);
        when(pedidoItemRepository.findByPedidoId(1L)).thenReturn(List.of());

        PedidoResponseDTO resultado = pedidoService.crear(request);

        assertThat(resultado.getUsuarioId()).isEqualTo(10L);
        assertThat(resultado.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
    }

    @Test
    void crear_publicaElEventoDeEstadoPendiente() {
        PedidoItemRequestDTO item = new PedidoItemRequestDTO(1L, 1, BigDecimal.valueOf(15000));
        PedidoRequestDTO request = new PedidoRequestDTO(10L, "Av. Siempre Viva 123", List.of(item));

        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);
        when(pedidoItemRepository.findByPedidoId(1L)).thenReturn(List.of());

        pedidoService.crear(request);

        verify(pedidoProducer).publicarCambioEstado(1L, 10L, EstadoPedido.PENDIENTE);
    }

    @Test
    void obtenerPorId_lanzaExcepcionSiNoExiste() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.obtenerPorId(99L))
                .isInstanceOf(PedidoNotFoundException.class);
    }

    @Test
    void actualizarEstado_cambiaElEstadoDelPedido() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoGuardado));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);
        when(pedidoItemRepository.findByPedidoId(1L)).thenReturn(List.of());

        PedidoResponseDTO resultado = pedidoService.actualizarEstado(1L, EstadoPedido.CONFIRMADO);

        assertThat(pedidoGuardado.getEstado()).isEqualTo(EstadoPedido.CONFIRMADO);
        assertThat(resultado).isNotNull();
        verify(pedidoProducer).publicarCambioEstado(1L, 10L, EstadoPedido.CONFIRMADO);
    }

    @Test
    void actualizarEstado_noPublicaEventoSiElEstadoNoCambia() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoGuardado));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);
        when(pedidoItemRepository.findByPedidoId(1L)).thenReturn(List.of());

        pedidoService.actualizarEstado(1L, EstadoPedido.PENDIENTE);

        verify(pedidoProducer, never()).publicarCambioEstado(any(), any(), any());
    }

    @Test
    void aplicarResultadoPago_aprobadoConfirmaUnPedidoPendiente() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoGuardado));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        pedidoService.aplicarResultadoPago(1L, "APROBADO");

        assertThat(pedidoGuardado.getEstado()).isEqualTo(EstadoPedido.CONFIRMADO);
        verify(pedidoProducer).publicarCambioEstado(1L, 10L, EstadoPedido.CONFIRMADO);
    }

    @Test
    void aplicarResultadoPago_aprobadoNoHaceNadaSiElPedidoYaEstabaConfirmado() {
        pedidoGuardado.setEstado(EstadoPedido.CONFIRMADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoGuardado));

        pedidoService.aplicarResultadoPago(1L, "APROBADO");

        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(pedidoProducer, never()).publicarCambioEstado(any(), any(), any());
    }

    @Test
    void aplicarResultadoPago_reembolsadoCancelaUnPedidoConfirmado() {
        pedidoGuardado.setEstado(EstadoPedido.CONFIRMADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoGuardado));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        pedidoService.aplicarResultadoPago(1L, "REEMBOLSADO");

        assertThat(pedidoGuardado.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
        verify(pedidoProducer).publicarCambioEstado(1L, 10L, EstadoPedido.CANCELADO);
    }

    @Test
    void aplicarResultadoPago_lanzaExcepcionSiElPedidoNoExiste() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.aplicarResultadoPago(99L, "APROBADO"))
                .isInstanceOf(PedidoNotFoundException.class);
    }
}