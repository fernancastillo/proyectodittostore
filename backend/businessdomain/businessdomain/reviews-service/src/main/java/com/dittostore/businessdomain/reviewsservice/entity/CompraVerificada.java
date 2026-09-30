package com.dittostore.businessdomain.reviewsservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Registro de que un usuario compro un producto en un pedido.
 * Se llena de forma asincrona con los eventos de pedidos y pagos (RabbitMQ).
 */
@Entity
@Table(name = "compras_verificadas", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"pedido_id", "producto_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompraVerificada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private boolean pagado;
}
