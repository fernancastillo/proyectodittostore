package com.dittostore.businessdomain.pedidosservice.messaging.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


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
    public TopicExchange pedidoTopicExchange() {
        return new TopicExchange(props.exchanges().pedidoTopic(), true, false);
    }

    @Bean
    public TopicExchange pagoTopicExchange() {
        return new TopicExchange(props.exchanges().pagoTopic(), true, false);
    }

    @Bean
    public DirectExchange pedidosDlxExchange() {
        return new DirectExchange(props.exchanges().dlx(), true, false);
    }

    @Bean
    public Queue pagoQueue() {
        return QueueBuilder.durable(props.queues().pago().name())
                .deadLetterExchange(props.exchanges().dlx())
                .deadLetterRoutingKey(props.queues().pago().dlq())
                .build();
    }

    @Bean
    public Queue pagoDlq() {
        return QueueBuilder.durable(props.queues().pago().dlq()).build();
    }

    @Bean
    public Binding pagoBinding() {
        return BindingBuilder.bind(pagoQueue()).to(pagoTopicExchange())
                .with(props.queues().pago().routingKey());
    }

    @Bean
    public Binding pagoDlqBinding() {
        return BindingBuilder.bind(pagoDlq()).to(pedidosDlxExchange())
                .with(props.queues().pago().dlq());
    }
}