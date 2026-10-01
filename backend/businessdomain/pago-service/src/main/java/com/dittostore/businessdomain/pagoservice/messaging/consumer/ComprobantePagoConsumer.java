package com.dittostore.businessdomain.pagoservice.messaging.consumer;

import com.dittostore.businessdomain.pagoservice.messaging.event.PagoEstadoEvent;
import com.dittostore.businessdomain.pagoservice.messaging.support.ManualAckHandler;
import com.dittostore.businessdomain.pagoservice.messaging.support.MensajeNoRecuperableException;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ComprobantePagoConsumer {

    private static final Logger log = LoggerFactory.getLogger(ComprobantePagoConsumer.class);

    private final ManualAckHandler ackHandler;

    public ComprobantePagoConsumer(ManualAckHandler ackHandler) {
        this.ackHandler = ackHandler;
    }

    @RabbitListener(queues = "${dittostore.rabbitmq.queues.comprobante.name}")
    public void recibir(Message message, Channel channel) throws IOException {
        ackHandler.procesar(message, channel, new ParameterizedTypeReference<PagoEstadoEvent>() {}, this::generarComprobante);
    }

    private void generarComprobante(PagoEstadoEvent evento) {
        if (evento.transaccionId() == null || evento.transaccionId().isBlank()) {
            throw new MensajeNoRecuperableException("El evento no trae transaccionId");
        }
        log.info("[COMPROBANTE] Generado COMP-{} | pedido={} | monto=${} | método={}",
                evento.transaccionId(), evento.pedidoId(), evento.monto(), evento.metodoPago());
    }
}