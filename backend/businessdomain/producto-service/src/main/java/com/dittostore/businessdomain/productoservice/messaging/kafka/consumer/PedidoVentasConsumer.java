package com.dittostore.businessdomain.productoservice.messaging.kafka.consumer;

import com.dittostore.businessdomain.productoservice.messaging.kafka.event.PedidoEventoKafka;
import com.dittostore.businessdomain.productoservice.messaging.kafka.support.EventoJson;
import com.dittostore.businessdomain.productoservice.messaging.kafka.support.MensajeNoRecuperableException;
import com.dittostore.businessdomain.productoservice.service.VentaService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PedidoVentasConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoVentasConsumer.class);
    private static final String ESTADO_VENTA = "CONFIRMADO";

    private final EventoJson json;
    private final VentaService ventaService;

    public PedidoVentasConsumer(EventoJson json, VentaService ventaService) {
        this.json = json;
        this.ventaService = ventaService;
    }

    @KafkaListener(
            topics = "${dittostore.kafka.topics.pedidos-eventos}",
            groupId = "${dittostore.kafka.consumer.group-ventas}")
    public void recibir(ConsumerRecord<String, String> registro) {
        PedidoEventoKafka evento = leer(registro);
        log.info("[PRODUCTO-VENTAS] partición={} offset={} key={} pedido={} estado={}",
                registro.partition(), registro.offset(), registro.key(), evento.pedidoId(), evento.estado());

        if (!ESTADO_VENTA.equals(evento.estado())) {
            return;
        }

        List<PedidoEventoKafka.Item> items = evento.items();
        if (items == null || items.isEmpty()) {
            log.warn("[PRODUCTO-VENTAS] El pedido {} confirmado no trae ítems; no hay ventas que contabilizar",
                    evento.pedidoId());
            return;
        }
        for (PedidoEventoKafka.Item item : items) {
            if (item.productoId() == null || item.cantidad() == null || item.cantidad() <= 0
                    || item.subtotal() == null) {
                throw new MensajeNoRecuperableException("El pedido " + evento.pedidoId() + " trae un ítem inválido");
            }
        }

        if (ventaService.registrarVentaPedido(evento)) {
            log.info("[PRODUCTO-VENTAS] Venta del pedido {} contabilizada ({} ítems)", evento.pedidoId(), items.size());
        } else {
            log.info("[PRODUCTO-VENTAS] El pedido {} ya estaba contabilizado; se ignora", evento.pedidoId());
        }
    }

    private PedidoEventoKafka leer(ConsumerRecord<String, String> registro) {
        String ubicacion = "partición " + registro.partition() + " offset " + registro.offset();
        if (registro.value() == null || registro.value().isBlank()) {
            throw new MensajeNoRecuperableException("Mensaje vacío en " + ubicacion);
        }
        PedidoEventoKafka evento;
        try {
            evento = json.desdeJson(registro.value(), PedidoEventoKafka.class);
        } catch (RuntimeException ex) {
            throw new MensajeNoRecuperableException("JSON inválido en " + ubicacion + ": " + ex.getMessage());
        }
        if (evento == null || evento.pedidoId() == null || evento.estado() == null) {
            throw new MensajeNoRecuperableException("El evento de " + ubicacion + " no trae pedidoId o estado");
        }
        return evento;
    }
}