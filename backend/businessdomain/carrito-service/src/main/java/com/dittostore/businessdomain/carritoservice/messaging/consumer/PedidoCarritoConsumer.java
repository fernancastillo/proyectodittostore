package com.dittostore.businessdomain.carritoservice.messaging.consumer;

import com.dittostore.businessdomain.carritoservice.entity.EstadoCarrito;
import com.dittostore.businessdomain.carritoservice.messaging.event.PedidoEstadoEvent;
import com.dittostore.businessdomain.carritoservice.messaging.support.ManualAckHandler;
import com.dittostore.businessdomain.carritoservice.messaging.support.MensajeNoRecuperableException;
import com.dittostore.businessdomain.carritoservice.service.CarritoService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PedidoCarritoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoCarritoConsumer.class);

    private final ManualAckHandler ackHandler;
    private final CarritoService carritoService;

    public PedidoCarritoConsumer(ManualAckHandler ackHandler, CarritoService carritoService) {
        this.ackHandler = ackHandler;
        this.carritoService = carritoService;
    }

    @RabbitListener(queues = "${dittostore.rabbitmq.queues.pedido.name}")
    public void recibir(Message message, Channel channel) throws IOException {
        ackHandler.procesar(message, channel, new ParameterizedTypeReference<PedidoEstadoEvent>() {}, this::procesar);
    }

    private void procesar(PedidoEstadoEvent evento) {
        if (evento.pedidoId() == null || evento.usuarioId() == null) {
            throw new MensajeNoRecuperableException("El evento de pedido no trae pedidoId o usuarioId");
        }

        if (!"CONFIRMADO".equalsIgnoreCase(evento.estado())) {
            log.info("[CARRITO-PEDIDO] Pedido {} cambió a {} (sin acción para el carrito)",
                    evento.pedidoId(), evento.estado());
            return;
        }

        var carrito = carritoService.obtenerOCrearActivoPorUsuario(evento.usuarioId());
        carritoService.vaciarItems(carrito.getId());
        carritoService.actualizarEstado(carrito.getId(), EstadoCarrito.CONVERTIDO);

        log.info("[CARRITO-PEDIDO] Pedido {} confirmado: carrito {} del usuario {} vaciado y marcado CONVERTIDO",
                evento.pedidoId(), carrito.getId(), evento.usuarioId());
    }
}