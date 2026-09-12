package com.dittostore.businessdomain.carritoservice.repository;

import com.dittostore.businessdomain.carritoservice.entity.Carrito;
import com.dittostore.businessdomain.carritoservice.entity.EstadoCarrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    List<Carrito> findByUsuarioId(Long usuarioId);

    Optional<Carrito> findByUsuarioIdAndEstado(Long usuarioId, EstadoCarrito estado);
}