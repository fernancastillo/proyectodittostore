package com.dittostore.businessdomain.pedidosservice.messaging.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dittostore.kafka")
public record DittoKafkaProperties(String bootstrapServers, Topics topics, Consumidor consumer, Retry retry) {

    public record Topics(String pedidosEventos, String pagosAlertas) {}

    public record Consumidor(String groupAlertas, int concurrency) {}

    public record Retry(int maxIntentos, long backoffMs) {}
}