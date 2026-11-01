package com.dittostore.businessdomain.productoservice.messaging.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dittostore.kafka")
public record DittoKafkaProperties(String bootstrapServers, Topics topics, Consumidor consumer, Retry retry) {

    public record Topics(String pedidosEventos) {}

    public record Consumidor(String groupVentas, int concurrency) {}

    public record Retry(int maxIntentos, long backoffMs) {}
}