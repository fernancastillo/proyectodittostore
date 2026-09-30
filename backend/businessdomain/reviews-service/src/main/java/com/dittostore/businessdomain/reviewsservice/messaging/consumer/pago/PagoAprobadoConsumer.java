package com.dittostore.businessdomain.reviewsservice.messaging.consumer.pago;

import com.dittostore.businessdomain.reviewsservice.messaging.event.PagoAprobadoEvent;
import com.dittostore.businessdomain.reviewsservice.messaging.support.MensajeAckHandler;
import com.dittostore.businessdomain.reviewsservice.service.CompraVerificadaService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Consumidor del dominio PAGO: marca como pagadas las compras del pedido cuando el pago se aprueba. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PagoAprobadoConsumer {

    private static final String CONTEXTO = "pago.aprobado";

    private final CompraVerificadaService compraVerificadaService;
    private final MensajeAckHandler ackHandler;

    @RabbitListener(queues = "${ditto.rabbit.queues.pago-aprobado}")
    public void consumir(PagoAprobadoEvent evento,
                         Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                         @Header(name = AmqpHeaders.REDELIVERED, defaultValue = "false") boolean redelivered)
            throws IOException {
        try {
            validar(evento);
            log.info("[{}] Pago {} recibido para el pedido {}",
                CONTEXTO, evento.getPagoId(), evento.getPedidoId());

            compraVerificadaService.marcarPagado(evento.getPedidoId());

            ackHandler.confirmar(channel, deliveryTag);
        } catch (Exception ex) {
            ackHandler.rechazar(channel, deliveryTag, redelivered, ex, CONTEXTO);
        }
    }

    private void validar(PagoAprobadoEvent evento) {
        if (evento == null || evento.getPedidoId() == null) {
            throw new IllegalArgumentException("El evento de pago requiere pedidoId");
        }
        if (!"APROBADO".equalsIgnoreCase(evento.getEstado())) {
            throw new IllegalArgumentException(
                "El pago del pedido " + evento.getPedidoId() + " no esta APROBADO (estado: " + evento.getEstado() + ")");
        }
    }
}
