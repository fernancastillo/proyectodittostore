package com.dittostore.businessdomain.pedidosservice.messaging.kafka.producer;

import com.dittostore.businessdomain.pedidosservice.entity.EstadoPedido;
import com.dittostore.businessdomain.pedidosservice.entity.Pedido;
import com.dittostore.businessdomain.pedidosservice.entity.PedidoItem;
import com.dittostore.businessdomain.pedidosservice.messaging.kafka.config.DittoKafkaProperties;
import com.dittostore.businessdomain.pedidosservice.messaging.kafka.event.PedidoKafkaEvent;
import com.dittostore.businessdomain.pedidosservice.messaging.kafka.support.EventoJson;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private SendResult<String, String> sendResult;

    @Mock
    private RecordMetadata metadata;

    private final EventoJson json = new EventoJson();
    private PedidoKafkaProducer producer;

    @BeforeEach
    void setUp() {
        DittoKafkaProperties props = new DittoKafkaProperties("localhost:9092",
                new DittoKafkaProperties.Topics("pedidos.eventos", "pagos.alertas"), null, null);
        producer = new PedidoKafkaProducer(kafkaTemplate, props, json);
    }

    private Pedido pedido() {
        return Pedido.builder()
                .id(7L)
                .usuarioId(10L)
                .estado(EstadoPedido.CONFIRMADO)
                .total(new BigDecimal("55000"))
                .fechaPedido(LocalDateTime.now())
                .direccionEnvio("Av. Siempre Viva 123")
                .build();
    }

    private List<PedidoItem> items() {
        return List.of(PedidoItem.builder()
                .pedidoId(7L)
                .productoId(1L)
                .cantidad(2)
                .precioUnitario(new BigDecimal("15000"))
                .subtotal(new BigDecimal("30000"))
                .build());
    }

    @Test
    void publicarCambioEstado_enviaAlTopicConKeyPedidoIdYElDetalleDeItems() {
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));
        when(sendResult.getRecordMetadata()).thenReturn(metadata);

        producer.publicarCambioEstado(pedido(), items());

        ArgumentCaptor<String> valor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("pedidos.eventos"), eq("7"), valor.capture());

        PedidoKafkaEvent evento = json.desdeJson(valor.getValue(), PedidoKafkaEvent.class);
        assertThat(evento.pedidoId()).isEqualTo(7L);
        assertThat(evento.usuarioId()).isEqualTo(10L);
        assertThat(evento.estado()).isEqualTo("CONFIRMADO");
        assertThat(evento.items()).hasSize(1);
        assertThat(evento.items().get(0).productoId()).isEqualTo(1L);
        assertThat(evento.items().get(0).cantidad()).isEqualTo(2);
    }

    @Test
    void publicarCambioEstado_siKafkaFalla_noPropagaLaExcepcion() {
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Kafka caído"));

        assertThatCode(() -> producer.publicarCambioEstado(pedido(), items()))
                .doesNotThrowAnyException();
    }

    @Test
    void publicarCambioEstado_conPedidoSinEstado_noPropagaNiEnvia() {
        Pedido sinEstado = Pedido.builder().id(8L).usuarioId(10L).build();

        assertThatCode(() -> producer.publicarCambioEstado(sinEstado, items()))
                .doesNotThrowAnyException();
        verifyNoInteractions(kafkaTemplate);
    }
}