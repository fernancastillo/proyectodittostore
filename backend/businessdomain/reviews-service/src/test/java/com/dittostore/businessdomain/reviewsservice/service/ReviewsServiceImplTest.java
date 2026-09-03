package com.dittostore.businessdomain.reviewsservice.service;

import com.dittostore.businessdomain.reviewsservice.dto.ReviewsRequestDTO;
import com.dittostore.businessdomain.reviewsservice.dto.ReviewsResponseDTO;
import com.dittostore.businessdomain.reviewsservice.entity.Reviews;
import com.dittostore.businessdomain.reviewsservice.exception.ReviewDuplicadoException;
import com.dittostore.businessdomain.reviewsservice.exception.ReviewNotFoundException;
import com.dittostore.businessdomain.reviewsservice.repository.ReviewsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewsServiceImplTest {

    @Mock
    private ReviewsRepository reviewsRepository;

    @InjectMocks
    private ReviewsServiceImpl reviewsService;

    private Reviews reviewExistente;
    private ReviewsRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        reviewExistente = Reviews.builder()
                .id(1L)
                .productoId(10L)
                .usuarioId(20L)
                .calificacion(5)
                .comentario("Excelente caja, todas las cartas en buen estado")
                .build();

        requestDTO = new ReviewsRequestDTO();
        requestDTO.setProductoId(10L);
        requestDTO.setUsuarioId(20L);
        requestDTO.setCalificacion(5);
        requestDTO.setComentario("Excelente caja, todas las cartas en buen estado");
    }

    @Test
    void crear_deberiaGuardarYRetornarReview() {
        when(reviewsRepository.findByProductoIdAndUsuarioId(10L, 20L)).thenReturn(Optional.empty());
        when(reviewsRepository.save(any(Reviews.class))).thenReturn(reviewExistente);

        ReviewsResponseDTO resultado = reviewsService.crear(requestDTO);

        assertNotNull(resultado);
        assertEquals(5, resultado.getCalificacion());
        verify(reviewsRepository, times(1)).save(any(Reviews.class));
    }

    @Test
    void crear_cuandoYaExisteReviewDelUsuarioParaEseProducto_deberiaLanzarExcepcion() {
        when(reviewsRepository.findByProductoIdAndUsuarioId(10L, 20L))
                .thenReturn(Optional.of(reviewExistente));

        assertThrows(ReviewDuplicadoException.class, () -> reviewsService.crear(requestDTO));
        verify(reviewsRepository, never()).save(any(Reviews.class));
    }

    @Test
    void obtenerPorId_cuandoNoExiste_deberiaLanzarExcepcion() {
        when(reviewsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () -> reviewsService.obtenerPorId(99L));
    }

    @Test
    void obtenerPorProducto_deberiaRetornarListaDeReviews() {
        when(reviewsRepository.findByProductoId(10L)).thenReturn(List.of(reviewExistente));

        List<ReviewsResponseDTO> resultado = reviewsService.obtenerPorProducto(10L);

        assertEquals(1, resultado.size());
        verify(reviewsRepository, times(1)).findByProductoId(10L);
    }

    @Test
    void eliminar_cuandoNoExiste_deberiaLanzarExcepcion() {
        when(reviewsRepository.existsById(99L)).thenReturn(false);

        assertThrows(ReviewNotFoundException.class, () -> reviewsService.eliminar(99L));
        verify(reviewsRepository, never()).deleteById(any());
    }

    @Test
    void calcularPromedio_conVariasReviews_deberiaRetornarElPromedioCorrecto() {
        Reviews review2 = Reviews.builder()
                .id(2L)
                .productoId(10L)
                .usuarioId(30L)
                .calificacion(3)
                .comentario("Buena pero cara")
                .build();

        when(reviewsRepository.findByProductoId(10L))
                .thenReturn(List.of(reviewExistente, review2)); // calificaciones 5 y 3

        Double promedio = reviewsService.calcularPromedio(10L);

        assertEquals(4.0, promedio);
    }

    @Test
    void calcularPromedio_sinReviews_deberiaRetornarCero() {
        when(reviewsRepository.findByProductoId(99L)).thenReturn(List.of());

        Double promedio = reviewsService.calcularPromedio(99L);

        assertEquals(0.0, promedio);
    }
}