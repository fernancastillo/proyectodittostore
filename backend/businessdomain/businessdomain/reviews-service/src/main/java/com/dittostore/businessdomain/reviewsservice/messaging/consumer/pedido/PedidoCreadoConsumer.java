package com.dittostore.businessdomain.reviewsservice.messaging.consumer.pedido;

import com.dittostore.businessdomain.reviewsservice.messaging.event.PedidoCreadoEvent;
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

/** Consumidor del dominio PEDIDO: registra los productos comprados en cada pedido nuevo. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoCreadoConsumer {

    private static final String CONTEXTO = "pedido.creado";

    private final CompraVerificadaService compraVerificadaService;
    private final MensajeAckHandler ackHandler;

    @RabbitListener(queues = "${ditto.rabbit.queues.pedido-creado}")
    public void consumir(PedidoCreadoEvent evento,
                         Channel channel,
                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                         @Header(name = AmqpHeaders.REDELIVERED, defaultValue = "false") boolean redelivered)
            throws IOException {
        try {
            validar(evento);
            log.info("[{}] Pedido {} recibido (usuario {}, {} items)",
                CONTEXTO, evento.getPedidoId(), evento.getUsuarioId(), evento.getItems().size());

            for (PedidoCreadoEvent.Item item : evento.getItems()) {
                compraVerificadaService.registrarCompra(
                    evento.getPedidoId(), evento.getUsuarioId(), item.getProductoId(), item.getCantidad());
            }

            ackHandler.confirmar(channel, deliveryTag);
        } catch (Exception ex) {
            ackHandler.rechazar(channel, deliveryTag, redelivered, ex, CONTEXTO);
        }
    }

    private void validar(PedidoCreadoEvent evento) {
        if (evento == null || evento.getPedidoId() == null || evento.getUsuarioId() == null) {
            throw new IllegalArgumentException("El evento de pedido requiere pedidoId y usuarioId");
        }
        if (evento.getItems() == null || evento.getItems().isEmpty()) {
            throw new IllegalArgumentException("El pedido " + evento.getPedidoId() + " no trae items");
        }
        for (PedidoCreadoEvent.Item item : evento.getItems()) {
            if (item.getProductoId() == null || item.getCantidad() == null || item.getCantidad() < 1) {
                throw new IllegalArgumentException("Item invalido en el pedido " + evento.getPedidoId());
            }
        }
    }
}
