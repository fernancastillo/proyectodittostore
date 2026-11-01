package com.dittostore.businessdomain.pagoservice.service;

import com.dittostore.businessdomain.pagoservice.dto.PagoResponseDTO;
import com.dittostore.businessdomain.pagoservice.entity.EstadoPago;
import com.dittostore.businessdomain.pagoservice.entity.MetodoPago;
import com.dittostore.businessdomain.pagoservice.messaging.kafka.config.DittoKafkaProperties;
import com.dittostore.businessdomain.pagoservice.messaging.kafka.event.AlertaPagoEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DetectorAlertasPagoTest {

    private final DetectorAlertasPago detector = new DetectorAlertasPago(
            new DittoKafkaProperties("localhost:9092",
                    new DittoKafkaProperties.Topics("pagos.alertas"),
                    new DittoKafkaProperties.Alertas(new BigDecimal("300000"))));

    private PagoResponseDTO pago(String monto, EstadoPago estado) {
        return PagoResponseDTO.builder()
                .id(3L)
                .pedidoId(7L)
                .monto(new BigDecimal(monto))
                .metodoPago(MetodoPago.TARJETA)
                .estado(estado)
                .transaccionId("TXN-1")
                .fechaPago(LocalDateTime.now())
                .build();
    }

    @Test
    void evaluarCreacion_conMontoSobreElUmbral_generaAlertaAlta() {
        Optional<AlertaPagoEvent> alerta = detector.evaluarCreacion(pago("500000", EstadoPago.PENDIENTE));

        assertThat(alerta).isPresent();
        assertThat(alerta.get().severidad()).isEqualTo("ALTA");
        assertThat(alerta.get().pedidoId()).isEqualTo(7L);
        assertThat(alerta.get().pagoId()).isEqualTo(3L);
        assertThat(alerta.get().motivo()).contains("umbral");
    }

    @Test
    void evaluarCreacion_conMontoIgualAlUmbral_noGeneraAlerta() {
        assertThat(detector.evaluarCreacion(pago("300000", EstadoPago.PENDIENTE))).isEmpty();
    }

    @Test
    void evaluarCreacion_conMontoBajo_noGeneraAlerta() {
        assertThat(detector.evaluarCreacion(pago("15000", EstadoPago.PENDIENTE))).isEmpty();
    }

    @Test
    void evaluarCreacion_noAplicaLaReglaDeRechazo() {
        assertThat(detector.evaluarCreacion(pago("15000", EstadoPago.RECHAZADO))).isEmpty();
    }

    @Test
    void evaluarCambioEstado_conPagoRechazado_generaAlertaMedia() {
        Optional<AlertaPagoEvent> alerta = detector.evaluarCambioEstado(pago("15000", EstadoPago.RECHAZADO));

        assertThat(alerta).isPresent();
        assertThat(alerta.get().severidad()).isEqualTo("MEDIA");
        assertThat(alerta.get().motivo()).contains("rechazado");
    }

    @Test
    void evaluarCambioEstado_conPagoAprobado_noGeneraAlerta() {
        assertThat(detector.evaluarCambioEstado(pago("500000", EstadoPago.APROBADO))).isEmpty();
    }
}