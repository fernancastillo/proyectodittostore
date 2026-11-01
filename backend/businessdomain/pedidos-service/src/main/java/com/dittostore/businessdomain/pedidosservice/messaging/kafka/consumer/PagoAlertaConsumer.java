package com.dittostore.businessdomain.pedidosservice.messaging.kafka.consumer;

import com.dittostore.businessdomain.pedidosservice.messaging.kafka.event.AlertaPagoKafkaEvent;
import com.dittostore.businessdomain.pedidosservice.messaging.kafka.support.EventoJson;
import com.dittostore.businessdomain.pedidosservice.messaging.kafka.support.MensajeNoRecuperableException;
import com.dittostore.businessdomain.pedidosservice.service.AlertaService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PagoAlertaConsumer {

    private static final Logger log = LoggerFactory.getLogger(PagoAlertaConsumer.class);

    private final EventoJson json;
    private final AlertaService alertaService;

    public PagoAlertaConsumer(EventoJson json, AlertaService alertaService) {
        this.json = json;
        this.alertaService = alertaService;
    }

    @KafkaListener(
            topics = "${dittostore.kafka.topics.pagos-alertas}",
            groupId = "${dittostore.kafka.consumer.group-alertas}")
    public void recibir(ConsumerRecord<String, String> registro) {
        AlertaPagoKafkaEvent evento = leer(registro);
        log.info("[PEDIDOS-ALERTAS] partición={} offset={} key={} pedido={} pago={} severidad={}",
                registro.partition(), registro.offset(), registro.key(),
                evento.pedidoId(), evento.pagoId(), evento.severidad());

        if (alertaService.registrarAlerta(evento)) {
            log.info("[PEDIDOS-ALERTAS] Alerta del pago {} registrada en el pedido {}",
                    evento.pagoId(), evento.pedidoId());
        }
    }

    private AlertaPagoKafkaEvent leer(ConsumerRecord<String, String> registro) {
        String ubicacion = "partición " + registro.partition() + " offset " + registro.offset();
        if (registro.value() == null || registro.value().isBlank()) {
            throw new MensajeNoRecuperableException("Mensaje vacío en " + ubicacion);
        }
        AlertaPagoKafkaEvent evento;
        try {
            evento = json.desdeJson(registro.value(), AlertaPagoKafkaEvent.class);
        } catch (RuntimeException ex) {
            throw new MensajeNoRecuperableException("JSON inválido en " + ubicacion + ": " + ex.getMessage());
        }
        if (evento == null || evento.pedidoId() == null || evento.pagoId() == null
                || evento.monto() == null || evento.severidad() == null
                || evento.motivo() == null || evento.motivo().isBlank()) {
            throw new MensajeNoRecuperableException("La alerta de " + ubicacion + " tiene campos obligatorios vacíos");
        }
        return evento;
    }
}