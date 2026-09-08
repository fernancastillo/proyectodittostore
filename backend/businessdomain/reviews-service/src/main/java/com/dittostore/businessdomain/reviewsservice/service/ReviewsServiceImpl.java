package com.dittostore.businessdomain.reviewsservice.service;

import com.dittostore.businessdomain.reviewsservice.dto.ReviewsRequestDTO;
import com.dittostore.businessdomain.reviewsservice.dto.ReviewsResponseDTO;
import com.dittostore.businessdomain.reviewsservice.entity.Reviews;
import com.dittostore.businessdomain.reviewsservice.exception.ReviewDuplicadoException;
import com.dittostore.businessdomain.reviewsservice.exception.ReviewNotFoundException;
import com.dittostore.businessdomain.reviewsservice.repository.ReviewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewsServiceImpl implements ReviewsService {

    private final ReviewsRepository reviewsRepository;

    @Override
    public List<ReviewsResponseDTO> obtenerTodas() {
        return reviewsRepository.findAll().stream()
            .map(this::toResponseDTO)
            .toList();
    }

    @Override
    public ReviewsResponseDTO obtenerPorId(Long id) {
        Reviews review = reviewsRepository.findById(id)
            .orElseThrow(() -> new ReviewNotFoundException("Review no encontrada con id: " + id));
        return toResponseDTO(review);
    }

    @Override
    public List<ReviewsResponseDTO> obtenerPorProducto(Long productoId) {
        return reviewsRepository.findByProductoId(productoId).stream()
            .map(this::toResponseDTO)
            .toList();
    }

    @Override
    public List<ReviewsResponseDTO> obtenerPorUsuario(Long usuarioId) {
        return reviewsRepository.findByUsuarioId(usuarioId).stream()
            .map(this::toResponseDTO)
            .toList();
    }

    @Override
    public ReviewsResponseDTO crear(ReviewsRequestDTO dto) {
        reviewsRepository.findByProductoIdAndUsuarioId(dto.getProductoId(), dto.getUsuarioId())
            .ifPresent(r -> {
                throw new ReviewDuplicadoException(
                    "El usuario " + dto.getUsuarioId() + " ya reseño el producto " + dto.getProductoId());
            });

        Reviews review = Reviews.builder()
            .productoId(dto.getProductoId())
            .usuarioId(dto.getUsuarioId())
            .calificacion(dto.getCalificacion())
            .comentario(dto.getComentario())
            .build();

        Reviews guardada = reviewsRepository.save(review);
        return toResponseDTO(guardada);
    }

    @Override
    public ReviewsResponseDTO actualizar(Long id, ReviewsRequestDTO dto) {
        Reviews review = reviewsRepository.findById(id)
            .orElseThrow(() -> new ReviewNotFoundException("Review no encontrada con id: " + id));

        review.setCalificacion(dto.getCalificacion());
        review.setComentario(dto.getComentario());

        Reviews actualizada = reviewsRepository.save(review);
        return toResponseDTO(actualizada);
    }

    @Override
    public void eliminar(Long id) {
        if (!reviewsRepository.existsById(id)) {
            throw new ReviewNotFoundException("Review no encontrada con id: " + id);
        }
        reviewsRepository.deleteById(id);
    }

    @Override
    public Double calcularPromedio(Long productoId) {
        List<Reviews> reviews = reviewsRepository.findByProductoId(productoId);
        return reviews.stream()
            .mapToInt(Reviews::getCalificacion)
            .average()
            .orElse(0.0);
    }

    private ReviewsResponseDTO toResponseDTO(Reviews review) {
        return ReviewsResponseDTO.builder()
            .id(review.getId())
            .productoId(review.getProductoId())
            .usuarioId(review.getUsuarioId())
            .calificacion(review.getCalificacion())
            .comentario(review.getComentario())
            .build();
    }
}