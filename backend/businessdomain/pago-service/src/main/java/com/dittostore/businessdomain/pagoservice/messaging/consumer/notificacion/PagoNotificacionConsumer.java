package com.dittostore.businessdomain.pagoservice.messaging.consumer.notificacion;

import com.dittostore.businessdomain.pagoservice.messaging.event.PagoEvent;
import com.dittostore.businessdomain.pagoservice.messaging.support.MensajeAckHandler;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagoNotificacionConsumer {

    private static final String CONTEXTO = "pago.notificacion";

    private final MensajeAckHandler ackHandler;

    @RabbitListener(queues = "${ditto.rabbit.queues.notificacion}")
    public void consumir(PagoEvent evento,
                         Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                         @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey,
                         @Header(name = AmqpHeaders.REDELIVERED, defaultValue = "false") boolean redelivered)
            throws IOException {
        try {
            validar(evento);
            log.info("[{}] Notificacion ({}): pago {} del pedido {} quedo en estado {}",
                    CONTEXTO, routingKey, evento.getPagoId(), evento.getPedidoId(), evento.getEstado());
            ackHandler.confirmar(channel, deliveryTag);
        } catch (Exception ex) {
            ackHandler.rechazar(channel, deliveryTag, redelivered, ex, CONTEXTO);
        }
    }

    private void validar(PagoEvent evento) {
        if (evento == null || evento.getPedidoId() == null || evento.getEstado() == null) {
            throw new IllegalArgumentException("El evento de pago requiere pedidoId y estado");
        }
    }
}