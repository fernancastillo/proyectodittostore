package com.dittostore.businessdomain.reviewsservice.repository;

import com.dittostore.businessdomain.reviewsservice.entity.Reviews;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewsRepository extends JpaRepository<Reviews, Long> {

    List<Reviews> findByProductoId(Long productoId);

    List<Reviews> findByUsuarioId(Long usuarioId);

    Optional<Reviews> findByProductoIdAndUsuarioId(Long productoId, Long usuarioId);
}