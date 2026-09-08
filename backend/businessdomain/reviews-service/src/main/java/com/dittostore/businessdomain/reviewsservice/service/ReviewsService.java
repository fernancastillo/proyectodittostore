package com.dittostore.businessdomain.reviewsservice.service;

import com.dittostore.businessdomain.reviewsservice.dto.ReviewsRequestDTO;
import com.dittostore.businessdomain.reviewsservice.dto.ReviewsResponseDTO;

import java.util.List;

public interface ReviewsService {

    List<ReviewsResponseDTO> obtenerTodas();

    ReviewsResponseDTO obtenerPorId(Long id);

    List<ReviewsResponseDTO> obtenerPorProducto(Long productoId);

    List<ReviewsResponseDTO> obtenerPorUsuario(Long usuarioId);

    ReviewsResponseDTO crear(ReviewsRequestDTO dto);

    ReviewsResponseDTO actualizar(Long id, ReviewsRequestDTO dto);

    void eliminar(Long id);

    Double calcularPromedio(Long productoId);
}