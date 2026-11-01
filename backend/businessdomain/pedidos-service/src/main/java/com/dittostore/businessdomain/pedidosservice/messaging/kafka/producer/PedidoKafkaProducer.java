package com.dittostore.businessdomain.pedidosservice.messaging.kafka.producer;

import com.dittostore.businessdomain.pedidosservice.entity.Pedido;
import com.dittostore.businessdomain.pedidosservice.entity.PedidoItem;
import com.dittostore.businessdomain.pedidosservice.messaging.kafka.config.DittoKafkaProperties;
import com.dittostore.businessdomain.pedidosservice.messaging.kafka.event.PedidoKafkaEvent;
import com.dittostore.businessdomain.pedidosservice.messaging.kafka.support.EventoJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PedidoKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(PedidoKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DittoKafkaProperties props;
    private final EventoJson json;

    public PedidoKafkaProducer(KafkaTemplate<String, String> kafkaTemplate, DittoKafkaProperties props,
                               EventoJson json) {
        this.kafkaTemplate = kafkaTemplate;
        this.props = props;
        this.json = json;
    }

    /** Publica el estado del pedido (con sus ítems) en pedidos.eventos; key = pedidoId para conservar el orden. */
    public void publicarCambioEstado(Pedido pedido, List<PedidoItem> items) {
        try {
            List<PedidoKafkaEvent.Item> detalle = items.stream()
                    .map(i -> new PedidoKafkaEvent.Item(i.getProductoId(), i.getCantidad(), i.getSubtotal()))
                    .toList();
            PedidoKafkaEvent evento = new PedidoKafkaEvent(pedido.getId(), pedido.getUsuarioId(),
                    pedido.getEstado().name(), pedido.getTotal(), detalle, LocalDateTime.now());
            enviarTrasCommit(String.valueOf(pedido.getId()), json.aJson(evento));
        } catch (Exception ex) {
            log.error("[KAFKA-PRODUCER] No se pudo preparar el evento del pedido {}: {}",
                    pedido.getId(), ex.getMessage());
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
        String topic = props.topics().pedidosEventos();
        try {
            kafkaTemplate.send(topic, key, valor).whenComplete((resultado, error) -> {
                if (error != null) {
                    log.error("[KAFKA-PRODUCER] Falló el envío topic='{}' key='{}': {}",
                            topic, key, error.getMessage());
                } else {
                    log.info("[KAFKA-PRODUCER] Evento publicado topic='{}' key='{}' partición={} offset={}",
                            topic, key, resultado.getRecordMetadata().partition(),
                            resultado.getRecordMetadata().offset());
                }
            });
        } catch (Exception ex) {
            log.error("[KAFKA-PRODUCER] No se pudo publicar topic='{}' key='{}': {}", topic, key, ex.getMessage());
        }
    }
}