package com.dittostore.businessdomain.pagoservice.messaging.support;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class MensajeAckHandler {

    public void confirmar(Channel channel, long deliveryTag) throws IOException {
        channel.basicAck(deliveryTag, false);
    }

    public void rechazar(Channel channel, long deliveryTag, boolean redelivered,
                         Exception error, String contexto) throws IOException {
        boolean noRecuperable = error instanceof IllegalArgumentException;

        if (!noRecuperable && !redelivered) {
            log.warn("[{}] Error recuperable, se reintenta una vez (requeue): {}", contexto, error.getMessage());
            channel.basicNack(deliveryTag, false, true);
            return;
        }

        log.error("[{}] Mensaje enviado a DLQ ({}): {}", contexto,
                noRecuperable ? "error no recuperable" : "reintento agotado", error.getMessage());
        channel.basicNack(deliveryTag, false, false);
    }
}