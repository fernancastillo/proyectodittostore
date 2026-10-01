package com.dittostore.businessdomain.reviewsservice.messaging.consumer;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class ReviewsDlqListener {

    private static final Logger log = LoggerFactory.getLogger(ReviewsDlqListener.class);

    @RabbitListener(
            queues = {
                    "${dittostore.rabbitmq.queues.pedido.dlq}",
                    "${dittostore.rabbitmq.queues.pago.dlq}"
            },
            autoStartup = "${dittostore.rabbitmq.dlq-listener.enabled:true}")
    public void recibir(Message message, Channel channel) throws IOException {
        MessageProperties mp = message.getMessageProperties();
        log.error("[DLQ-REVIEWS] Mensaje muerto en '{}' | origen: {} | body={}",
                mp.getConsumerQueue(), describirOrigen(mp),
                new String(message.getBody(), StandardCharsets.UTF_8));
        channel.basicAck(mp.getDeliveryTag(), false);
    }

    private String describirOrigen(MessageProperties mp) {
        List<Map<String, ?>> xDeath = mp.getXDeathHeader();
        if (xDeath == null || xDeath.isEmpty()) {
            return "sin cabecera x-death";
        }
        Map<String, ?> primero = xDeath.get(0);
        return "cola=" + primero.get("queue") + ", razón=" + primero.get("reason") + ", veces=" + primero.get("count");
    }
}