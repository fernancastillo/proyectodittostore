package com.dittostore.businessdomain.reviewsservice.messaging.config;

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

    // ---------- Exchanges ----------

    @Bean
    public TopicExchange pedidoTopicExchange() {
        return new TopicExchange(props.exchanges().pedidoTopic(), true, false);
    }

    @Bean
    public TopicExchange pagoTopicExchange() {
        return new TopicExchange(props.exchanges().pagoTopic(), true, false);
    }

    @Bean
    public DirectExchange reviewsDlxExchange() {
        return new DirectExchange(props.exchanges().dlx(), true, false);
    }

    // ---------- Pedido ----------

    @Bean
    public Queue pedidoQueue() {
        return colaConDlq(props.queues().pedido());
    }

    @Bean
    public Queue pedidoDlq() {
        return QueueBuilder.durable(props.queues().pedido().dlq()).build();
    }

    @Bean
    public Binding pedidoBinding() {
        return BindingBuilder.bind(pedidoQueue()).to(pedidoTopicExchange())
                .with(props.queues().pedido().routingKey());
    }

    @Bean
    public Binding pedidoDlqBinding() {
        return BindingBuilder.bind(pedidoDlq()).to(reviewsDlxExchange())
                .with(props.queues().pedido().dlq());
    }

    // ---------- Pago ----------

    @Bean
    public Queue pagoQueue() {
        return colaConDlq(props.queues().pago());
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
        return BindingBuilder.bind(pagoDlq()).to(reviewsDlxExchange())
                .with(props.queues().pago().dlq());
    }

    private Queue colaConDlq(RabbitMQProperties.Cola cola) {
        return QueueBuilder.durable(cola.name())
                .deadLetterExchange(props.exchanges().dlx())
                .deadLetterRoutingKey(cola.dlq())
                .build();
    }
}