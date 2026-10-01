package com.dittostore.businessdomain.pagoservice.messaging.producer;

import com.dittostore.businessdomain.pagoservice.dto.PagoResponseDTO;

public interface PagoEventPublisher {

    void publicarCambioEstado(PagoResponseDTO pago);

    void solicitarReembolso(Long pagoId, String motivo);
}