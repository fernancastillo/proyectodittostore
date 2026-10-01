package com.dittostore.businessdomain.reviewsservice.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dittostore.rabbitmq")
public record RabbitMQProperties(Exchanges exchanges, Queues queues, Retry retry) {

    public record Exchanges(String pedidoTopic, String pagoTopic, String dlx) {}

    public record Queues(Cola pedido, Cola pago) {}

    public record Cola(String name, String dlq, String routingKey) {}

    public record Retry(int maxIntentos, long backoffMs) {}
}