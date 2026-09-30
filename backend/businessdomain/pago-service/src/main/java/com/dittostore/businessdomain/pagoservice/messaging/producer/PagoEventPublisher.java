package com.dittostore.businessdomain.pagoservice.messaging.producer;

import com.dittostore.businessdomain.pagoservice.dto.PagoResponseDTO;
import com.dittostore.businessdomain.pagoservice.messaging.config.DittoRabbitProperties;
import com.dittostore.businessdomain.pagoservice.messaging.event.PagoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagoEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final DittoRabbitProperties props;

    public void publicarCreado(PagoResponseDTO pago) {
        enviar(props.getRoutingKeys().getPagoCreado(), pago);
    }

    /** Publica pago.aprobado o pago.rechazado; los demas estados no generan evento. */
    public void publicarCambioEstado(PagoResponseDTO pago) {
        switch (pago.getEstado()) {
            case APROBADO -> enviar(props.getRoutingKeys().getPagoAprobado(), pago);
            case RECHAZADO -> enviar(props.getRoutingKeys().getPagoRechazado(), pago);
            default -> log.debug("Estado {} no publica evento", pago.getEstado());
        }
    }

    private void enviar(String routingKey, PagoResponseDTO pago) {
        PagoEvent evento = PagoEvent.builder()
                .pagoId(pago.getId())
                .pedidoId(pago.getPedidoId())
                .monto(pago.getMonto())
                .metodoPago(pago.getMetodoPago().name())
                .estado(pago.getEstado().name())
                .transaccionId(pago.getTransaccionId())
                .build();

        Runnable envio = () -> {
            try {
                rabbitTemplate.convertAndSend(props.getExchanges().getPagos(), routingKey, evento);
                log.info("[PRODUCTOR] Evento {} publicado para el pago {} (pedido {})",
                        routingKey, evento.getPagoId(), evento.getPedidoId());
            } catch (Exception ex) {
                log.error("[PRODUCTOR] No se pudo publicar {} del pago {}: {}",
                        routingKey, evento.getPagoId(), ex.getMessage());
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    envio.run();
                }
            });
        } else {
            envio.run();
        }
    }
}