package com.dittostore.businessdomain.pagoservice.messaging.consumer;

import com.dittostore.businessdomain.pagoservice.messaging.event.PagoEstadoEvent;
import com.dittostore.businessdomain.pagoservice.messaging.support.ManualAckHandler;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NotificacionPagoConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacionPagoConsumer.class);

    private final ManualAckHandler ackHandler;

    public NotificacionPagoConsumer(ManualAckHandler ackHandler) {
        this.ackHandler = ackHandler;
    }

    @RabbitListener(queues = "${dittostore.rabbitmq.queues.notificacion.name}")
    public void recibir(Message message, Channel channel) throws IOException {
        ackHandler.procesar(message, channel, new ParameterizedTypeReference<PagoEstadoEvent>() {}, this::notificar);
    }

    private void notificar(PagoEstadoEvent evento) {
        log.info("[NOTIFICACION] Pedido {}: el pago {} ({}) cambió a {} por ${}",
                evento.pedidoId(), evento.pagoId(), evento.metodoPago(), evento.estado(), evento.monto());
    }
}