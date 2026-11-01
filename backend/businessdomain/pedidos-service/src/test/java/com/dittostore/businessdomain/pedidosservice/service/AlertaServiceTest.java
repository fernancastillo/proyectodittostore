package com.dittostore.businessdomain.pedidosservice.service;

import com.dittostore.businessdomain.pedidosservice.dto.AlertaPedidoResponseDTO;
import com.dittostore.businessdomain.pedidosservice.entity.PedidoAlerta;
import com.dittostore.businessdomain.pedidosservice.messaging.kafka.event.AlertaPagoKafkaEvent;
import com.dittostore.businessdomain.pedidosservice.repository.PedidoAlertaRepository;
import com.dittostore.businessdomain.pedidosservice.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private PedidoAlertaRepository alertaRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private AlertaServiceImpl alertaService;

    private AlertaPagoKafkaEvent evento() {
        return new AlertaPagoKafkaEvent(3L, 7L, new BigDecimal("500000"), "TARJETA", "PENDIENTE",
                "TXN-1", "Monto inusualmente alto", "ALTA", LocalDateTime.of(2026, 11, 1, 10, 0));
    }

    @Test
    void registrarAlerta_siElPedidoNoExiste_descartaLaAlerta() {
        when(pedidoRepository.existsById(7L)).thenReturn(false);

        assertThat(alertaService.registrarAlerta(evento())).isFalse();
        verify(alertaRepository, never()).save(any(PedidoAlerta.class));
    }

    @Test
    void registrarAlerta_siYaEstabaRegistrada_laIgnora() {
        when(pedidoRepository.existsById(7L)).thenReturn(true);
        when(alertaRepository.existsByPagoIdAndMotivo(3L, "Monto inusualmente alto")).thenReturn(true);

        assertThat(alertaService.registrarAlerta(evento())).isFalse();
        verify(alertaRepository, never()).save(any(PedidoAlerta.class));
    }

    @Test
    void registrarAlerta_conPedidoExistenteYAlertaNueva_laGuarda() {
        when(pedidoRepository.existsById(7L)).thenReturn(true);
        when(alertaRepository.existsByPagoIdAndMotivo(3L, "Monto inusualmente alto")).thenReturn(false);

        assertThat(alertaService.registrarAlerta(evento())).isTrue();

        ArgumentCaptor<PedidoAlerta> captor = ArgumentCaptor.forClass(PedidoAlerta.class);
        verify(alertaRepository).save(captor.capture());
        assertThat(captor.getValue().getPedidoId()).isEqualTo(7L);
        assertThat(captor.getValue().getPagoId()).isEqualTo(3L);
        assertThat(captor.getValue().getSeveridad()).isEqualTo("ALTA");
        assertThat(captor.getValue().getMonto()).isEqualByComparingTo("500000");
    }

    @Test
    void listar_convierteLasEntidadesADto() {
        PedidoAlerta alerta = PedidoAlerta.builder()
                .id(1L).pedidoId(7L).pagoId(3L).monto(new BigDecimal("500000"))
                .motivo("Monto inusualmente alto").severidad("ALTA")
                .fechaAlerta(LocalDateTime.of(2026, 11, 1, 10, 0)).build();
        when(alertaRepository.findAllByOrderByFechaAlertaDesc()).thenReturn(List.of(alerta));

        List<AlertaPedidoResponseDTO> resultado = alertaService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).pedidoId()).isEqualTo(7L);
        assertThat(resultado.get(0).severidad()).isEqualTo("ALTA");
    }
}