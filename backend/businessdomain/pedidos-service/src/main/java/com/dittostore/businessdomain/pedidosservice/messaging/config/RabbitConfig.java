package com.dittostore.businessdomain.pedidosservice.messaging.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    private final RabbitMQProperties properties;

    public RabbitConfig(RabbitMQProperties properties) {
        this.properties = properties;
    }

    @Bean
    public TopicExchange pedidosExchange() {
        return new TopicExchange(properties.getExchange().getPedidos(), true, false);
    }
}