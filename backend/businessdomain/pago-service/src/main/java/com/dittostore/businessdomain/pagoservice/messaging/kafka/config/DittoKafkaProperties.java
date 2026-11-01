package com.dittostore.businessdomain.pagoservice.messaging.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "dittostore.kafka")
public record DittoKafkaProperties(String bootstrapServers, Topics topics, Alertas alertas) {

    public record Topics(String pagosAlertas) {}

    public record Alertas(BigDecimal montoUmbral) {}
}