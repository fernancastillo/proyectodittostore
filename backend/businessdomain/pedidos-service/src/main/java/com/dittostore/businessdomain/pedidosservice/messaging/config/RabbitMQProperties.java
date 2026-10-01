package com.dittostore.businessdomain.pedidosservice.messaging.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitMQProperties {

    private Exchange exchange = new Exchange();
    private RoutingKey routingKey = new RoutingKey();

    @Getter
    @Setter
    public static class Exchange {
        private String pedidos;
    }

    @Getter
    @Setter
    public static class RoutingKey {
        private String pedidoConfirmado;
    }
}