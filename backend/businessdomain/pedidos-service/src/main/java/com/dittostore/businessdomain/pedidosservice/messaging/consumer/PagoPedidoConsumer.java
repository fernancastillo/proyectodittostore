package com.dittostore.businessdomain.pedidosservice.messaging.consumer;

import com.dittostore.businessdomain.pedidosservice.messaging.event.PagoEstadoEvent;
import com.dittostore.businessdomain.pedidosservice.messaging.support.ManualAckHandler;
import com.dittostore.businessdomain.pedidosservice.messaging.support.MensajeNoRecuperableException;
import com.dittostore.businessdomain.pedidosservice.service.PedidoService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PagoPedidoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PagoPedidoConsumer.class);

    private final ManualAckHandler ackHandler;
    private final PedidoService pedidoService;

    public PagoPedidoConsumer(ManualAckHandler ackHandler, PedidoService pedidoService) {
        this.ackHandler = ackHandler;
        this.pedidoService = pedidoService;
    }

    @RabbitListener(queues = "${dittostore.rabbitmq.queues.pago.name}")
    public void recibir(Message message, Channel channel) throws IOException {
        ackHandler.procesar(message, channel, new ParameterizedTypeReference<PagoEstadoEvent>() {}, this::procesar);
    }

    private void procesar(PagoEstadoEvent evento) {
        if (evento.pedidoId() == null || evento.estado() == null) {
            throw new MensajeNoRecuperableException("El evento de pago no trae pedidoId o estado");
        }
        log.info("[PEDIDOS-PAGO] Pago {} del pedido {} cambió a {}", evento.pagoId(), evento.pedidoId(), evento.estado());
        pedidoService.aplicarResultadoPago(evento.pedidoId(), evento.estado());
    }
}