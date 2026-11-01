package com.dittostore.businessdomain.pagoservice.messaging.kafka.producer;

import com.dittostore.businessdomain.pagoservice.messaging.kafka.config.DittoKafkaProperties;
import com.dittostore.businessdomain.pagoservice.messaging.kafka.event.AlertaPagoEvent;
import com.dittostore.businessdomain.pagoservice.messaging.kafka.support.EventoJson;
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
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaPagoKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private SendResult<String, String> sendResult;

    @Mock
    private RecordMetadata metadata;

    private final EventoJson json = new EventoJson();
    private AlertaPagoKafkaProducer producer;

    @BeforeEach
    void setUp() {
        DittoKafkaProperties props = new DittoKafkaProperties("localhost:9092",
                new DittoKafkaProperties.Topics("pagos.alertas"),
                new DittoKafkaProperties.Alertas(new BigDecimal("300000")));
        producer = new AlertaPagoKafkaProducer(kafkaTemplate, props, json);
    }

    private AlertaPagoEvent alerta() {
        return new AlertaPagoEvent(3L, 7L, new BigDecimal("500000"), "TARJETA", "PENDIENTE", "TXN-1",
                "Monto inusualmente alto", "ALTA", LocalDateTime.now());
    }

    @Test
    void publicar_enviaAlTopicDeAlertasConKeyPedidoId() {
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));
        when(sendResult.getRecordMetadata()).thenReturn(metadata);

        producer.publicar(alerta());

        ArgumentCaptor<String> valor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("pagos.alertas"), eq("7"), valor.capture());

        AlertaPagoEvent enviada = json.desdeJson(valor.getValue(), AlertaPagoEvent.class);
        assertThat(enviada.pagoId()).isEqualTo(3L);
        assertThat(enviada.pedidoId()).isEqualTo(7L);
        assertThat(enviada.severidad()).isEqualTo("ALTA");
        assertThat(enviada.monto()).isEqualByComparingTo("500000");
    }

    @Test
    void publicar_siKafkaFalla_noPropagaLaExcepcion() {
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Kafka caído"));

        assertThatCode(() -> producer.publicar(alerta())).doesNotThrowAnyException();
    }
}