package com.dittostore.businessdomain.productoservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "venta_pedido_procesado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PedidoVentaProcesado {

    @Id
    @Column(name = "pedido_id")
    private Long pedidoId;

    @Column(name = "fecha_procesado", nullable = false)
    private LocalDateTime fechaProcesado;
}