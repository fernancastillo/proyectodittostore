package com.dittostore.businessdomain.reviewsservice.messaging.consumer;

import com.dittostore.businessdomain.reviewsservice.messaging.event.PagoEstadoEvent;
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
public class PagoReviewsConsumer {

    private static final Logger log = LoggerFactory.getLogger(PagoReviewsConsumer.class);

    private final ManualAckHandler ackHandler;

    public PagoReviewsConsumer(ManualAckHandler ackHandler) {
        this.ackHandler = ackHandler;
    }

    @RabbitListener(queues = "${dittostore.rabbitmq.queues.pago.name}")
    public void recibir(Message message, Channel channel) throws IOException {
        ackHandler.procesar(message, channel, new ParameterizedTypeReference<PagoEstadoEvent>() {}, this::procesar);
    }

    private void procesar(PagoEstadoEvent evento) {
        if (evento.pedidoId() == null) {
            throw new MensajeNoRecuperableException("El evento de pago no trae pedidoId");
        }
        if ("APROBADO".equalsIgnoreCase(evento.estado())) {
            log.info("[REVIEWS-PAGO] Pago {} aprobado para el pedido {}: compra verificada para reseñas",
                    evento.pagoId(), evento.pedidoId());
        } else {
            log.info("[REVIEWS-PAGO] Pago {} del pedido {} cambió a {} (sin acción para reseñas)",
                    evento.pagoId(), evento.pedidoId(), evento.estado());
        }
    }
}