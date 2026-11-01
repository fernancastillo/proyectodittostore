package com.dittostore.businessdomain.pagoservice.service;

import com.dittostore.businessdomain.pagoservice.dto.PagoResponseDTO;
import com.dittostore.businessdomain.pagoservice.entity.EstadoPago;
import com.dittostore.businessdomain.pagoservice.messaging.kafka.config.DittoKafkaProperties;
import com.dittostore.businessdomain.pagoservice.messaging.kafka.event.AlertaPagoEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class DetectorAlertasPago {

    private final BigDecimal montoUmbral;

    public DetectorAlertasPago(DittoKafkaProperties props) {
        this.montoUmbral = props.alertas().montoUmbral();
    }

    public Optional<AlertaPagoEvent> evaluarCreacion(PagoResponseDTO pago) {
        if (pago.getMonto() != null && pago.getMonto().compareTo(montoUmbral) > 0) {
            String motivo = "Monto inusualmente alto: " + pago.getMonto().toPlainString()
                    + " supera el umbral de " + montoUmbral.toPlainString();
            return Optional.of(construir(pago, motivo, "ALTA"));
        }
        return Optional.empty();
    }

    public Optional<AlertaPagoEvent> evaluarCambioEstado(PagoResponseDTO pago) {
        if (pago.getEstado() == EstadoPago.RECHAZADO) {
            return Optional.of(construir(pago, "Pago rechazado (método " + pago.getMetodoPago() + ")", "MEDIA"));
        }
        return Optional.empty();
    }

    private AlertaPagoEvent construir(PagoResponseDTO pago, String motivo, String severidad) {
        return new AlertaPagoEvent(
                pago.getId(),
                pago.getPedidoId(),
                pago.getMonto(),
                String.valueOf(pago.getMetodoPago()),
                String.valueOf(pago.getEstado()),
                pago.getTransaccionId(),
                motivo,
                severidad,
                LocalDateTime.now());
    }
}