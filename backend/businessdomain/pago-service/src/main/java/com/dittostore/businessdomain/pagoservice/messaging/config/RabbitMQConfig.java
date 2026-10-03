package com.dittostore.businessdomain.pagoservice.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.SimpleMessageConverter;

@Configuration
@EnableConfigurationProperties(RabbitMQProperties.class)
public class RabbitMQConfig {

    private final RabbitMQProperties props;

    public RabbitMQConfig(RabbitMQProperties props) {
        this.props = props;
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new SimpleMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setDefaultRequeueRejected(false);
        factory.setPrefetchCount(1);
        return factory;
    }

    @Bean
    public DirectExchange pagoDirectExchange() {
        return new DirectExchange(props.exchanges().direct(), true, false);
    }

    @Bean
    public TopicExchange pagoTopicExchange() {
        return new TopicExchange(props.exchanges().topic(), true, false);
    }

    @Bean
    public DirectExchange pagoDlxExchange() {
        return new DirectExchange(props.exchanges().dlx(), true, false);
    }

    @Bean
    public Queue notificacionQueue() {
        return colaConDlq(props.queues().notificacion());
    }

    @Bean
    public Queue notificacionDlq() {
        return QueueBuilder.durable(props.queues().notificacion().dlq()).build();
    }

    @Bean
    public Binding notificacionBinding() {
        return BindingBuilder.bind(notificacionQueue()).to(pagoTopicExchange())
                .with(props.queues().notificacion().routingKey());
    }

    @Bean
    public Binding notificacionDlqBinding() {
        return BindingBuilder.bind(notificacionDlq()).to(pagoDlxExchange())
                .with(props.queues().notificacion().dlq());
    }

    @Bean
    public Queue comprobanteQueue() {
        return colaConDlq(props.queues().comprobante());
    }

    @Bean
    public Queue comprobanteDlq() {
        return QueueBuilder.durable(props.queues().comprobante().dlq()).build();
    }

    @Bean
    public Binding comprobanteBinding() {
        return BindingBuilder.bind(comprobanteQueue()).to(pagoTopicExchange())
                .with(props.queues().comprobante().routingKey());
    }

    @Bean
    public Binding comprobanteDlqBinding() {
        return BindingBuilder.bind(comprobanteDlq()).to(pagoDlxExchange())
                .with(props.queues().comprobante().dlq());
    }

    @Bean
    public Queue reembolsoQueue() {
        return colaConDlq(props.queues().reembolso());
    }

    @Bean
    public Queue reembolsoDlq() {
        return QueueBuilder.durable(props.queues().reembolso().dlq()).build();
    }

    @Bean
    public Binding reembolsoBinding() {
        return BindingBuilder.bind(reembolsoQueue()).to(pagoDirectExchange())
                .with(props.queues().reembolso().routingKey());
    }

    @Bean
    public Binding reembolsoDlqBinding() {
        return BindingBuilder.bind(reembolsoDlq()).to(pagoDlxExchange())
                .with(props.queues().reembolso().dlq());
    }

    private Queue colaConDlq(RabbitMQProperties.Cola cola) {
        return QueueBuilder.durable(cola.name())
                .deadLetterExchange(props.exchanges().dlx())
                .deadLetterRoutingKey(cola.dlq())
                .build();
    }
}