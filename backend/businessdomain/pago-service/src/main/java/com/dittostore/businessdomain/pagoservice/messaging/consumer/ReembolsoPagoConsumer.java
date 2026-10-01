package com.dittostore.businessdomain.pagoservice.messaging.consumer;

import com.dittostore.businessdomain.pagoservice.messaging.event.ReembolsoSolicitadoMessage;
import com.dittostore.businessdomain.pagoservice.messaging.support.ManualAckHandler;
import com.dittostore.businessdomain.pagoservice.messaging.support.MensajeNoRecuperableException;
import com.dittostore.businessdomain.pagoservice.service.PagoService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ReembolsoPagoConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReembolsoPagoConsumer.class);

    private final ManualAckHandler ackHandler;
    private final PagoService pagoService;

    public ReembolsoPagoConsumer(ManualAckHandler ackHandler, PagoService pagoService) {
        this.ackHandler = ackHandler;
        this.pagoService = pagoService;
    }

    @RabbitListener(queues = "${dittostore.rabbitmq.queues.reembolso.name}")
    public void recibir(Message message, Channel channel) throws IOException {
        ackHandler.procesar(message, channel, new ParameterizedTypeReference<ReembolsoSolicitadoMessage>() {}, this::procesar);
    }

    private void procesar(ReembolsoSolicitadoMessage mensaje) {
        if (mensaje.pagoId() == null) {
            throw new MensajeNoRecuperableException("El mensaje no trae pagoId");
        }
        log.info("[REEMBOLSO] Procesando reembolso del pago {} (motivo: {})", mensaje.pagoId(), mensaje.motivo());
        pagoService.procesarReembolso(mensaje.pagoId());
    }
}