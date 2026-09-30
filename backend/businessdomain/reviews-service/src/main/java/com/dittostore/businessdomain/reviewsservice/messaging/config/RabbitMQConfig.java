package com.dittostore.businessdomain.reviewsservice.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Toda la topologia RabbitMQ de reviews-service vive aqui.
 * Los nombres NO estan escritos en codigo: se leen de application.properties (prefijo ditto.rabbit).
 *
 * Rutas de mensajeria:
 *   pedidos.exchange --(pedido.creado)--> reviews.pedido-creado.queue --> PedidoCreadoConsumer
 *   pagos.exchange   --(pago.aprobado)--> reviews.pago-aprobado.queue --> PagoAprobadoConsumer
 *   Mensaje rechazado -> reviews.dlx (fanout) -> reviews.dlq --> DeadLetterListener
 */
@Configuration
public class RabbitMQConfig {

    @Value("${ditto.rabbit.exchanges.pedidos}")
    private String exchangePedidos;

    @Value("${ditto.rabbit.exchanges.pagos}")
    private String exchangePagos;

    @Value("${ditto.rabbit.exchanges.dlx}")
    private String exchangeDlx;

    @Value("${ditto.rabbit.queues.pedido-creado}")
    private String colaPedidoCreado;

    @Value("${ditto.rabbit.queues.pago-aprobado}")
    private String colaPagoAprobado;

    @Value("${ditto.rabbit.queues.dlq}")
    private String colaDlq;

    @Value("${ditto.rabbit.routing-keys.pedido-creado}")
    private String routingKeyPedidoCreado;

    @Value("${ditto.rabbit.routing-keys.pago-aprobado}")
    private String routingKeyPagoAprobado;

    // ---------- Exchanges ----------

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(exchangePedidos, true, false);
    }

    @Bean
    public TopicExchange pagosExchange() {
        return new TopicExchange(exchangePagos, true, false);
    }

    @Bean
    public FanoutExchange deadLetterExchange() {
        return new FanoutExchange(exchangeDlx, true, false);
    }

    // ---------- Colas ----------

    @Bean
    public Queue pedidoCreadoQueue() {
        return QueueBuilder.durable(colaPedidoCreado)
                .deadLetterExchange(exchangeDlx)
                .build();
    }

    @Bean
    public Queue pagoAprobadoQueue() {
        return QueueBuilder.durable(colaPagoAprobado)
                .deadLetterExchange(exchangeDlx)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(colaDlq).build();
    }

    // ---------- Bindings ----------

    @Bean
    public Binding pedidoCreadoBinding() {
        return BindingBuilder.bind(pedidoCreadoQueue())
                .to(pedidosExchange())
                .with(routingKeyPedidoCreado);
    }

    @Bean
    public Binding pagoAprobadoBinding() {
        return BindingBuilder.bind(pagoAprobadoQueue())
                .to(pagosExchange())
                .with(routingKeyPagoAprobado);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange());
    }

    // ---------- Conversion JSON (usada por los @RabbitListener) ----------

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
