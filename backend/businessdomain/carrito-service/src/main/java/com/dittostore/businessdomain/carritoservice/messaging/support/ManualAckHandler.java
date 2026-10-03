package com.dittostore.businessdomain.carritoservice.messaging.support;

import com.dittostore.businessdomain.carritoservice.exception.CarritoItemNotFoundException;
import com.dittostore.businessdomain.carritoservice.exception.CarritoNotFoundException;
import com.dittostore.businessdomain.carritoservice.messaging.config.RabbitMQProperties;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Component
public class ManualAckHandler {

    private static final Logger log = LoggerFactory.getLogger(ManualAckHandler.class);
    public static final String HEADER_REINTENTOS = "x-retry-count";

    private final JacksonJsonMessageConverter converter;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties props;

    public ManualAckHandler(JacksonJsonMessageConverter converter, RabbitTemplate rabbitTemplate,
                             RabbitMQProperties props) {
        this.converter = converter;
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    @SuppressWarnings("unchecked")
    public <T> void procesar(Message message, Channel channel,
                              ParameterizedTypeReference<T> tipo, Consumer<T> accion) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();

        T payload;
        try {
            payload = (T) converter.fromMessage(message, tipo);
        } catch (MessageConversionException ex) {
            enviarADlq(message, channel, "formato inválido: " + ex.getMessage());
            return;
        }
        if (payload == null) {
            enviarADlq(message, channel, "cuerpo vacío");
            return;
        }

        try {
            accion.accept(payload);
        } catch (Exception ex) {
            manejarError(message, channel, ex);
            return;
        }
        channel.basicAck(tag, false);
    }

    private void manejarError(Message message, Channel channel, Exception ex) throws IOException {
        if (esNoRecuperable(ex)) {
            enviarADlq(message, channel,
                    "error no recuperable (" + ex.getClass().getSimpleName() + "): " + ex.getMessage());
        } else {
            reintentarOEnviarADlq(message, channel, ex);
        }
    }

    private boolean esNoRecuperable(Exception ex) {
        return ex instanceof MensajeNoRecuperableException
                || ex instanceof CarritoNotFoundException
                || ex instanceof CarritoItemNotFoundException
                || ex instanceof IllegalArgumentException
                || ex instanceof IllegalStateException;
    }

    private void reintentarOEnviarADlq(Message message, Channel channel, Exception ex) throws IOException {
        MessageProperties mp = message.getMessageProperties();
        long tag = mp.getDeliveryTag();
        int intentos = intentosPrevios(mp);
        int maximo = props.retry().maxIntentos();

        if (intentos >= maximo) {
            enviarADlq(message, channel, "reintentos agotados (" + maximo + "): " + ex.getMessage());
            return;
        }

        log.warn("[REINTENTO] cola='{}' intento {}/{} por error recuperable: {}",
                mp.getConsumerQueue(), intentos + 1, maximo, ex.getMessage());
        esperar(props.retry().backoffMs() * (intentos + 1));

        try {
            mp.setHeader(HEADER_REINTENTOS, intentos + 1);
            rabbitTemplate.send("", mp.getConsumerQueue(), message);
            channel.basicAck(tag, false);
        } catch (AmqpException e) {
            log.error("[REINTENTO] No se pudo reencolar, el mensaje vuelve a la cola: {}", e.getMessage());
            channel.basicNack(tag, false, true);
        }
    }

    private void enviarADlq(Message message, Channel channel, String motivo) throws IOException {
        MessageProperties mp = message.getMessageProperties();
        log.error("[DLQ] Mensaje descartado de la cola '{}' -> {} | body={}",
                mp.getConsumerQueue(), motivo, new String(message.getBody(), StandardCharsets.UTF_8));
        channel.basicNack(mp.getDeliveryTag(), false, false);
    }

    private int intentosPrevios(MessageProperties mp) {
        Object valor = mp.getHeader(HEADER_REINTENTOS);
        return valor instanceof Number n ? n.intValue() : 0;
    }

    private void esperar(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}