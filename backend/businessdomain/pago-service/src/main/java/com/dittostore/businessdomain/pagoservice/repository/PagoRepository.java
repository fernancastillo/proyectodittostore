package com.dittostore.businessdomain.pagoservice.repository;

import com.dittostore.businessdomain.pagoservice.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByPedidoId(Long pedidoId);

    Optional<Pago> findByTransaccionId(String transaccionId);
}
