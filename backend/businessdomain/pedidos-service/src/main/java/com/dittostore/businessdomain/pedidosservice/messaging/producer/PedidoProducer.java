package com.dittostore.businessdomain.pedidosservice.messaging.producer;

import com.dittostore.businessdomain.pedidosservice.messaging.config.RabbitMQProperties;
import com.dittostore.businessdomain.pedidosservice.messaging.dto.PedidoConfirmadoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PedidoProducer {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties properties;

    public PedidoProducer(RabbitTemplate rabbitTemplate, RabbitMQProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    public void publicarPedidoConfirmado(PedidoConfirmadoEvent evento) {
        String exchange = properties.getExchange().getPedidos();
        String routingKey = properties.getRoutingKey().getPedidoConfirmado();

        log.info("Publicando evento pedido.confirmado para pedidoId={}", evento.getPedidoId());
        rabbitTemplate.convertAndSend(exchange, routingKey, evento);
    }
}