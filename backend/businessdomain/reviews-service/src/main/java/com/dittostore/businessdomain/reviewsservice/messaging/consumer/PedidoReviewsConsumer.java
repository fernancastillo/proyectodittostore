package com.dittostore.businessdomain.reviewsservice.messaging.consumer;

import com.dittostore.businessdomain.reviewsservice.messaging.event.PedidoEstadoEvent;
import com.dittostore.businessdomain.reviewsservice.messaging.support.ManualAckHandler;
import com.dittostore.businessdomain.reviewsservice.messaging.support.MensajeNoRecuperableException;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PedidoReviewsConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoReviewsConsumer.class);

    private final ManualAckHandler ackHandler;

    public PedidoReviewsConsumer(ManualAckHandler ackHandler) {
        this.ackHandler = ackHandler;
    }

    @RabbitListener(queues = "${dittostore.rabbitmq.queues.pedido.name}")
    public void recibir(Message message, Channel channel) throws IOException {
        ackHandler.procesar(message, channel, new ParameterizedTypeReference<PedidoEstadoEvent>() {}, this::procesar);
    }

    private void procesar(PedidoEstadoEvent evento) {
        if (evento.pedidoId() == null || evento.usuarioId() == null) {
            throw new MensajeNoRecuperableException("El evento de pedido no trae pedidoId o usuarioId");
        }
        if ("ENTREGADO".equalsIgnoreCase(evento.estado())) {
            log.info("[REVIEWS-PEDIDO] Pedido {} entregado: el usuario {} ya puede reseñar los productos comprados",
                    evento.pedidoId(), evento.usuarioId());
        } else {
            log.info("[REVIEWS-PEDIDO] Pedido {} cambió a {} (sin acción para reseñas)",
                    evento.pedidoId(), evento.estado());
        }
    }
}