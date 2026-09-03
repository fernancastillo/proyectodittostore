package com.dittostore.businessdomain.carritoservice.repository;

import com.dittostore.businessdomain.carritoservice.entity.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    List<Carrito> findByUsuarioId(Long usuarioId);
}