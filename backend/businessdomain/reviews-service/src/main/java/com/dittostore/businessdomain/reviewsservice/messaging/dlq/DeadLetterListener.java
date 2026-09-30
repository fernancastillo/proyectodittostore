package com.dittostore.businessdomain.reviewsservice.messaging.dlq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Escucha la DLQ y deja en logs cada mensaje que no pudo procesarse. Siempre hace ACK para no ciclar. */
@Slf4j
@Component
public class DeadLetterListener {

    @RabbitListener(queues = "${ditto.rabbit.queues.dlq}")
    public void registrar(Message mensaje,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            MessageProperties props = mensaje.getMessageProperties();
            Map<String, Object> headers = props.getHeaders();

            log.error("[DLQ] Mensaje no procesado | colaOrigen={} | motivo={} | exchangeOrigen={} | routingKey={} | cuerpo={}",
                headers.get("x-first-death-queue"),
                headers.get("x-first-death-reason"),
                headers.get("x-first-death-exchange"),
                props.getReceivedRoutingKey(),
                new String(mensaje.getBody(), StandardCharsets.UTF_8));
        } finally {
            channel.basicAck(deliveryTag, false);
        }
    }
}
