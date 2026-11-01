package com.dittostore.businessdomain.pedidosservice.repository;

import com.dittostore.businessdomain.pedidosservice.entity.PedidoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoAlertaRepository extends JpaRepository<PedidoAlerta, Long> {

    boolean existsByPagoIdAndMotivo(Long pagoId, String motivo);

    List<PedidoAlerta> findAllByOrderByFechaAlertaDesc();

    List<PedidoAlerta> findByPedidoIdOrderByFechaAlertaDesc(Long pedidoId);
}