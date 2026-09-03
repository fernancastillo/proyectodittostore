package com.dittostore.infrastructure.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class RouteConfig {

    @Bean
    public RouterFunction<ServerResponse> bffRoute() {
        return route("bff_route")
            .route(request -> request.path().startsWith("/bff"), http())
            .filter(lb("bff-service"))
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> productoRoute() {
        return route("producto_route")
            .route(request -> request.path().startsWith("/api/productos"), http())
            .filter(lb("producto-service"))
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> usuariosRoute() {
        return route("usuarios_route")
            .route(request -> request.path().startsWith("/api/usuarios"), http())
            .filter(lb("usuarios-service"))
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> carritoRoute() {
        return route("carrito_route")
            .route(request -> request.path().startsWith("/api/carritos"), http())
            .filter(lb("carrito-service"))
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> pedidosRoute() {
        return route("pedidos_route")
            .route(request -> request.path().startsWith("/api/pedidos"), http())
            .filter(lb("pedidos-service"))
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> pagoRoute() {
        return route("pago_route")
            .route(request -> request.path().startsWith("/api/pagos"), http())
            .filter(lb("pago-service"))
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> reviewsRoute() {
        return route("reviews_route")
            .route(request -> request.path().startsWith("/api/reviews"), http())
            .filter(lb("reviews-service"))
            .build();
    }
}