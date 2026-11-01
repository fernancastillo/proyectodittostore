package com.dittostore.businessdomain.pagoservice.messaging.kafka.producer;

import com.dittostore.businessdomain.pagoservice.messaging.kafka.config.DittoKafkaProperties;
import com.dittostore.businessdomain.pagoservice.messaging.kafka.event.AlertaPagoEvent;
import com.dittostore.businessdomain.pagoservice.messaging.kafka.support.EventoJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class AlertaPagoKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(AlertaPagoKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DittoKafkaProperties props;
    private final EventoJson json;

    public AlertaPagoKafkaProducer(KafkaTemplate<String, String> kafkaTemplate, DittoKafkaProperties props,
                                   EventoJson json) {
        this.kafkaTemplate = kafkaTemplate;
        this.props = props;
        this.json = json;
    }

    public void publicar(AlertaPagoEvent alerta) {
        try {
            enviarTrasCommit(String.valueOf(alerta.pedidoId()), json.aJson(alerta));
        } catch (Exception ex) {
            log.error("[KAFKA-PRODUCER] No se pudo preparar la alerta del pago {}: {}",
                    alerta.pagoId(), ex.getMessage());
        }
    }

    private void enviarTrasCommit(String key, String valor) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviar(key, valor);
                }
            });
        } else {
            enviar(key, valor);
        }
    }

    private void enviar(String key, String valor) {
        String topic = props.topics().pagosAlertas();
        try {
            kafkaTemplate.send(topic, key, valor).whenComplete((resultado, error) -> {
                if (error != null) {
                    log.error("[KAFKA-PRODUCER] Falló el envío de la alerta topic='{}' key='{}': {}",
                            topic, key, error.getMessage());
                } else {
                    log.info("[KAFKA-PRODUCER] Alerta publicada topic='{}' key='{}' partición={} offset={}",
                            topic, key, resultado.getRecordMetadata().partition(),
                            resultado.getRecordMetadata().offset());
                }
            });
        } catch (Exception ex) {
            log.error("[KAFKA-PRODUCER] No se pudo publicar la alerta topic='{}' key='{}': {}",
                    topic, key, ex.getMessage());
        }
    }
}