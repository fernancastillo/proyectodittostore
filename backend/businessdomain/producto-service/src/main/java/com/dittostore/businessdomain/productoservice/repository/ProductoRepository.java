package com.dittostore.businessdomain.productoservice.repository;

import com.dittostore.businessdomain.productoservice.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}