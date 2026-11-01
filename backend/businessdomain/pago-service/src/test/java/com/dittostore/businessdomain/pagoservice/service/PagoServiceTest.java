package com.dittostore.businessdomain.pagoservice.service;

import com.dittostore.businessdomain.pagoservice.dto.PagoRequestDTO;
import com.dittostore.businessdomain.pagoservice.dto.PagoResponseDTO;
import com.dittostore.businessdomain.pagoservice.entity.EstadoPago;
import com.dittostore.businessdomain.pagoservice.entity.MetodoPago;
import com.dittostore.businessdomain.pagoservice.entity.Pago;
import com.dittostore.businessdomain.pagoservice.exception.PagoNotFoundException;
import com.dittostore.businessdomain.pagoservice.repository.PagoRepository;
import com.dittostore.businessdomain.pagoservice.messaging.producer.PagoEventPublisher;
import com.dittostore.businessdomain.pagoservice.messaging.kafka.producer.AlertaPagoKafkaProducer;
import com.dittostore.businessdomain.pagoservice.messaging.kafka.event.AlertaPagoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PagoEventPublisher pagoEventPublisher;

    @Mock
    private DetectorAlertasPago detectorAlertasPago;

    @Mock
    private AlertaPagoKafkaProducer alertaPagoKafkaProducer;

    @InjectMocks
    private PagoServiceImpl pagoService;

    private Pago pagoGuardado;

    @BeforeEach
    void setUp() {
        pagoGuardado = Pago.builder()
                .id(1L)
                .pedidoId(5L)
                .monto(BigDecimal.valueOf(45000))
                .metodoPago(MetodoPago.TARJETA)
                .estado(EstadoPago.PENDIENTE)
                .transaccionId("TXN-ABC12345")
                .fechaPago(LocalDateTime.now())
                .build();
    }

    @Test
    void crear_generaUnTransaccionIdYQuedaEnEstadoPendiente() {
        PagoRequestDTO request = new PagoRequestDTO(5L, BigDecimal.valueOf(45000), MetodoPago.TARJETA);

        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoGuardado);

        PagoResponseDTO resultado = pagoService.crear(request);

        assertThat(resultado.getPedidoId()).isEqualTo(5L);
        assertThat(resultado.getEstado()).isEqualTo(EstadoPago.PENDIENTE);
        assertThat(resultado.getTransaccionId()).isNotBlank();
    }

    @Test
    void obtenerPorId_lanzaExcepcionSiNoExiste() {
        when(pagoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagoService.obtenerPorId(99L))
                .isInstanceOf(PagoNotFoundException.class);
    }

    @Test
    void actualizarEstado_cambiaElEstadoDelPago() {
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoGuardado));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoGuardado);

        PagoResponseDTO resultado = pagoService.actualizarEstado(1L, EstadoPago.APROBADO);

        assertThat(pagoGuardado.getEstado()).isEqualTo(EstadoPago.APROBADO);
        assertThat(resultado).isNotNull();
    }

    @Test
    void crear_publicaLaAlertaCuandoElDetectorLaGenera() {
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoGuardado);
        AlertaPagoEvent alerta = new AlertaPagoEvent(1L, 1L, BigDecimal.valueOf(500000), "TARJETA",
                "PENDIENTE", "TXN-1", "Monto inusualmente alto", "ALTA", LocalDateTime.now());
        when(detectorAlertasPago.evaluarCreacion(any(PagoResponseDTO.class))).thenReturn(Optional.of(alerta));

        pagoService.crear(new PagoRequestDTO(1L, BigDecimal.valueOf(500000), MetodoPago.TARJETA));

        verify(alertaPagoKafkaProducer).publicar(alerta);
    }

    @Test
    void crear_noPublicaAlertaSiElDetectorNoDetectaNada() {
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoGuardado);

        pagoService.crear(new PagoRequestDTO(1L, BigDecimal.valueOf(1000), MetodoPago.TARJETA));

        verify(alertaPagoKafkaProducer, never()).publicar(any());
    }
}
