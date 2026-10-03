package com.dittostore.businessdomain.pedidosservice.messaging.support;

import com.dittostore.businessdomain.pedidosservice.exception.PedidoNotFoundException;
import com.dittostore.businessdomain.pedidosservice.messaging.config.RabbitMQProperties;
import com.dittostore.businessdomain.pedidosservice.messaging.event.PagoEstadoEvent;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.core.ParameterizedTypeReference;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ManualAckHandlerTest {

    private static final String COLA = "pedidos.pago.queue";
    private static final String JSON_VALIDO = "{\"pagoId\":1,\"pedidoId\":5,\"monto\":1000,"
            + "\"metodoPago\":\"TARJETA\",\"estado\":\"APROBADO\",\"transaccionId\":\"TXN-1\","
            + "\"fechaEvento\":\"2026-10-01T10:00:00\"}";
    private static final ParameterizedTypeReference<PagoEstadoEvent> TIPO = new ParameterizedTypeReference<>() {};

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private Channel channel;

    private ManualAckHandler handler;

    @BeforeEach
    void setUp() {
        RabbitMQProperties props = new RabbitMQProperties(
                new RabbitMQProperties.Exchanges("pedido.topic.exchange", "pago.topic.exchange", "pedidos.dlx.exchange"),
                new RabbitMQProperties.RoutingKeys("pedido.estado"),
                new RabbitMQProperties.Queues(new RabbitMQProperties.Cola(COLA, "pedidos.pago.dlq", "pago.estado.*")),
                new RabbitMQProperties.Retry(2, 0));
        handler = new ManualAckHandler(new JacksonJsonMessageConverter(), rabbitTemplate, props);
    }

    private Message mensaje(String cuerpo, Integer reintentos) {
        MessageProperties mp = new MessageProperties();
        mp.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        mp.setDeliveryTag(1L);
        mp.setConsumerQueue(COLA);
        if (reintentos != null) {
            mp.setHeader(ManualAckHandler.HEADER_REINTENTOS, reintentos);
        }
        return new Message(cuerpo.getBytes(StandardCharsets.UTF_8), mp);
    }

    @Test
    void mensajeValido_seProcesaYSeConfirmaConAck() throws Exception {
        AtomicBoolean procesado = new AtomicBoolean(false);

        handler.procesar(mensaje(JSON_VALIDO, null), channel, TIPO, evento -> {
            assertThat(evento.pedidoId()).isEqualTo(5L);
            procesado.set(true);
        });

        assertThat(procesado).isTrue();
        verify(channel).basicAck(1L, false);
    }

    @Test
    void jsonInvalido_vaALaDlqSinProcesarse() throws Exception {
        AtomicBoolean procesado = new AtomicBoolean(false);

        handler.procesar(mensaje("esto no es json", null), channel, TIPO, evento -> procesado.set(true));

        assertThat(procesado).isFalse();
        verify(channel).basicNack(1L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    void errorNoRecuperable_vaDirectoALaDlqSinReintentos() throws Exception {
        handler.procesar(mensaje(JSON_VALIDO, null), channel, TIPO, evento -> {
            throw new PedidoNotFoundException(5L);
        });

        verify(channel).basicNack(1L, false, false);
        verify(rabbitTemplate, never()).send(any(String.class), any(String.class), any(Message.class));
    }

    @Test
    void errorRecuperable_reencolaElMensajeYLoConfirma() throws Exception {
        handler.procesar(mensaje(JSON_VALIDO, null), channel, TIPO, evento -> {
            throw new RuntimeException("BD caída");
        });

        verify(rabbitTemplate).send(eq(""), eq(COLA), any(Message.class));
        verify(channel).basicAck(1L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void reintentosAgotados_vaALaDlq() throws Exception {
        handler.procesar(mensaje(JSON_VALIDO, 2), channel, TIPO, evento -> {
            throw new RuntimeException("BD caída");
        });

        verify(channel).basicNack(1L, false, false);
        verify(rabbitTemplate, never()).send(any(String.class), any(String.class), any(Message.class));
    }
}