package com.dittostore.businessdomain.reviewsservice.repository;

import com.dittostore.businessdomain.reviewsservice.entity.CompraVerificada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompraVerificadaRepository extends JpaRepository<CompraVerificada, Long> {

    List<CompraVerificada> findByPedidoId(Long pedidoId);

    boolean existsByPedidoIdAndProductoId(Long pedidoId, Long productoId);
}
