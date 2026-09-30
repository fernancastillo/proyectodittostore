package com.dittostore.businessdomain.reviewsservice.messaging.support;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Politica unica de ACK/NACK para todos los consumidores (ACK manual).
 *
 *  - Exito                         -> basicAck
 *  - Error no recuperable          -> basicNack sin requeue  -> DLQ
 *    (IllegalArgumentException: mensaje invalido, reintentar no sirve)
 *  - Error recuperable, 1er intento -> basicNack con requeue  -> se reintenta una vez
 *  - Error recuperable, reintento   -> basicNack sin requeue  -> DLQ
 */
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
