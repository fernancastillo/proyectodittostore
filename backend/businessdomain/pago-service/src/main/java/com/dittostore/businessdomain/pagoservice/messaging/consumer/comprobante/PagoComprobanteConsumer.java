package com.dittostore.businessdomain.pagoservice.messaging.consumer.comprobante;

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
public class PagoComprobanteConsumer {

    private static final String CONTEXTO = "pago.comprobante";

    private final MensajeAckHandler ackHandler;

    @RabbitListener(queues = "${ditto.rabbit.queues.comprobante}")
    public void consumir(PagoEvent evento,
                         Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                         @Header(name = AmqpHeaders.REDELIVERED, defaultValue = "false") boolean redelivered)
            throws IOException {
        try {
            validar(evento);
            String numero = "CMP-" + evento.getPedidoId() + "-" + evento.getTransaccionId();
            log.info("[{}] Comprobante {} generado | monto={} | metodo={}",
                    CONTEXTO, numero, evento.getMonto(), evento.getMetodoPago());
            ackHandler.confirmar(channel, deliveryTag);
        } catch (Exception ex) {
            ackHandler.rechazar(channel, deliveryTag, redelivered, ex, CONTEXTO);
        }
    }

    private void validar(PagoEvent evento) {
        if (evento == null || evento.getPedidoId() == null || evento.getTransaccionId() == null) {
            throw new IllegalArgumentException("El evento de pago requiere pedidoId y transaccionId");
        }
        if (!"APROBADO".equalsIgnoreCase(evento.getEstado())) {
            throw new IllegalArgumentException("No se emite comprobante: estado " + evento.getEstado());
        }
    }
}