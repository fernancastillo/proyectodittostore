package com.dittostore.businessdomain.pagoservice.messaging.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DittoRabbitProperties.class)
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final DittoRabbitProperties props;

    @Bean
    public TopicExchange pagosExchange() {
        return new TopicExchange(props.getExchanges().getPagos(), true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(props.getExchanges().getDlx(), true, false);
    }


    @Bean
    public Queue notificacionQueue() {
        return QueueBuilder.durable(props.getQueues().getNotificacion())
                .deadLetterExchange(props.getExchanges().getDlx())
                .deadLetterRoutingKey(props.getRoutingKeys().getDlqNotificacion())
                .build();
    }

    @Bean
    public Queue comprobanteQueue() {
        return QueueBuilder.durable(props.getQueues().getComprobante())
                .deadLetterExchange(props.getExchanges().getDlx())
                .deadLetterRoutingKey(props.getRoutingKeys().getDlqComprobante())
                .build();
    }

    @Bean
    public Queue rechazadoQueue() {
        return QueueBuilder.durable(props.getQueues().getRechazado())
                .deadLetterExchange(props.getExchanges().getDlx())
                .deadLetterRoutingKey(props.getRoutingKeys().getDlqRechazado())
                .build();
    }


    @Bean
    public Queue dlqNotificacion() {
        return QueueBuilder.durable(props.getQueues().getDlqNotificacion()).build();
    }

    @Bean
    public Queue dlqComprobante() {
        return QueueBuilder.durable(props.getQueues().getDlqComprobante()).build();
    }

    @Bean
    public Queue dlqRechazado() {
        return QueueBuilder.durable(props.getQueues().getDlqRechazado()).build();
    }


    @Bean
    public Binding notificacionBinding() {
        return BindingBuilder.bind(notificacionQueue()).to(pagosExchange())
                .with(props.getRoutingKeys().getPagoTodos());
    }

    @Bean
    public Binding comprobanteBinding() {
        return BindingBuilder.bind(comprobanteQueue()).to(pagosExchange())
                .with(props.getRoutingKeys().getPagoAprobado());
    }

    @Bean
    public Binding rechazadoBinding() {
        return BindingBuilder.bind(rechazadoQueue()).to(pagosExchange())
                .with(props.getRoutingKeys().getPagoRechazado());
    }


    @Bean
    public Binding dlqNotificacionBinding() {
        return BindingBuilder.bind(dlqNotificacion()).to(deadLetterExchange())
                .with(props.getRoutingKeys().getDlqNotificacion());
    }

    @Bean
    public Binding dlqComprobanteBinding() {
        return BindingBuilder.bind(dlqComprobante()).to(deadLetterExchange())
                .with(props.getRoutingKeys().getDlqComprobante());
    }

    @Bean
    public Binding dlqRechazadoBinding() {
        return BindingBuilder.bind(dlqRechazado()).to(deadLetterExchange())
                .with(props.getRoutingKeys().getDlqRechazado());
    }


    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}