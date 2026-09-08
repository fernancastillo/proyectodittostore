package com.dittostore.businessdomain.pedidosservice.repository;

import com.dittostore.businessdomain.pedidosservice.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioId(Long usuarioId);
}
