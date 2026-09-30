package com.dittostore.businessdomain.pagoservice.messaging.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ditto.rabbit")
public class DittoRabbitProperties {

    private Exchanges exchanges = new Exchanges();
    private Queues queues = new Queues();
    private RoutingKeys routingKeys = new RoutingKeys();

    @Data
    public static class Exchanges {
        private String pagos;
        private String dlx;
    }

    @Data
    public static class Queues {
        private String notificacion;
        private String comprobante;
        private String rechazado;
        private String dlqNotificacion;
        private String dlqComprobante;
        private String dlqRechazado;
    }

    @Data
    public static class RoutingKeys {
        private String pagoCreado;
        private String pagoAprobado;
        private String pagoRechazado;
        private String pagoTodos;
        private String dlqNotificacion;
        private String dlqComprobante;
        private String dlqRechazado;
    }
}