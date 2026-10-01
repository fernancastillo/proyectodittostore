package com.dittostore.businessdomain.pagoservice.messaging.producer;

import com.dittostore.businessdomain.pagoservice.dto.PagoResponseDTO;
import com.dittostore.businessdomain.pagoservice.messaging.config.RabbitMQProperties;
import com.dittostore.businessdomain.pagoservice.messaging.event.PagoEstadoEvent;
import com.dittostore.businessdomain.pagoservice.messaging.event.ReembolsoSolicitadoMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Component
public class PagoEventPublisherImpl implements PagoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PagoEventPublisherImpl.class);

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties props;

    public PagoEventPublisherImpl(RabbitTemplate rabbitTemplate, RabbitMQProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    @Override
    public void publicarCambioEstado(PagoResponseDTO pago) {
        PagoEstadoEvent evento = new PagoEstadoEvent(
                pago.getId(), pago.getPedidoId(), pago.getMonto(), pago.getMetodoPago(),
                pago.getEstado(), pago.getTransaccionId(), LocalDateTime.now());

        String routingKey = props.routingKeys().estadoPrefix() + "." + pago.getEstado().name().toLowerCase();
        enviarEventoTrasCommit(props.exchanges().topic(), routingKey, evento);
    }

    @Override
    public void solicitarReembolso(Long pagoId, String motivo) {
        ReembolsoSolicitadoMessage mensaje = new ReembolsoSolicitadoMessage(pagoId, motivo, LocalDateTime.now());
        rabbitTemplate.convertAndSend(props.exchanges().direct(), props.queues().reembolso().routingKey(), mensaje);
        log.info("[PRODUCER] Reembolso encolado para pago {}", pagoId);
    }

    private void enviarEventoTrasCommit(String exchange, String routingKey, Object evento) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviarEvento(exchange, routingKey, evento);
                }
            });
        } else {
            enviarEvento(exchange, routingKey, evento);
        }
    }

    private void enviarEvento(String exchange, String routingKey, Object evento) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, evento);
            log.info("[PRODUCER] Evento publicado exchange='{}' routingKey='{}'", exchange, routingKey);
        } catch (AmqpException ex) {
            log.error("[PRODUCER] No se pudo publicar exchange='{}' routingKey='{}': {}",
                    exchange, routingKey, ex.getMessage());
        }
    }
}