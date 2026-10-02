package com.dittostore.businessdomain.pedidosservice.messaging.producer;

import com.dittostore.businessdomain.pedidosservice.entity.EstadoPedido;
import com.dittostore.businessdomain.pedidosservice.messaging.config.RabbitMQProperties;
import com.dittostore.businessdomain.pedidosservice.messaging.event.PedidoEstadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Component
public class PedidoProducer {

    private static final Logger log = LoggerFactory.getLogger(PedidoProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties props;

    public PedidoProducer(RabbitTemplate rabbitTemplate, RabbitMQProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    public void publicarCambioEstado(Long pedidoId, Long usuarioId, EstadoPedido estado) {
        PedidoEstadoEvent evento = new PedidoEstadoEvent(pedidoId, usuarioId, estado.name(), LocalDateTime.now());
        String routingKey = props.routingKeys().estadoPrefix() + "." + estado.name().toLowerCase();
        enviarTrasCommit(props.exchanges().pedidoTopic(), routingKey, evento);
    }

    private void enviarTrasCommit(String exchange, String routingKey, Object evento) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviar(exchange, routingKey, evento);
                }
            });
        } else {
            enviar(exchange, routingKey, evento);
        }
    }

    private void enviar(String exchange, String routingKey, Object evento) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, evento);
            log.info("[PRODUCER] Evento publicado exchange='{}' routingKey='{}'", exchange, routingKey);
        } catch (AmqpException ex) {
            log.error("[PRODUCER] No se pudo publicar exchange='{}' routingKey='{}': {}",
                    exchange, routingKey, ex.getMessage());
        }
    }
}