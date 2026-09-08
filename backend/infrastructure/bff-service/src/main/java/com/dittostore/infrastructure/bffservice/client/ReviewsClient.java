package com.dittostore.infrastructure.bffservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "reviews-service")
public interface ReviewsClient {

    @GetMapping("/api/reviews/producto/{productoId}")
    Object obtenerReviewsPorProducto(@PathVariable Long productoId);

    @GetMapping("/api/reviews/usuario/{usuarioId}")
    Object obtenerReviewsPorUsuario(@PathVariable Long usuarioId);

    @GetMapping("/api/reviews/producto/{productoId}/promedio")
    Double obtenerPromedioPorProducto(@PathVariable Long productoId);
}