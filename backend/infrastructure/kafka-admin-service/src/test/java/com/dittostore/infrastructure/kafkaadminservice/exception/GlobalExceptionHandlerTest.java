package com.dittostore.infrastructure.kafkaadminservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void recursoNoEncontrado_responde404ConElMensaje() {
        ResponseEntity<Map<String, Object>> respuesta =
                handler.handleNoEncontrado(new RecursoNoEncontradoException("no existe"));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
        assertThat(respuesta.getBody()).containsEntry("mensaje", "no existe");
    }

    @Test
    void recursoExistente_responde409() {
        ResponseEntity<Map<String, Object>> respuesta =
                handler.handleExistente(new RecursoExistenteException("ya existe"));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void solicitudInvalida_responde400() {
        ResponseEntity<Map<String, Object>> respuesta =
                handler.handleSolicitudInvalida(new SolicitudInvalidaException("dato inválido"));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
        assertThat(respuesta.getBody()).containsEntry("mensaje", "dato inválido");
    }

    @Test
    void errorDeKafka_responde502() {
        ResponseEntity<Map<String, Object>> respuesta =
                handler.handleKafkaAdmin(new KafkaAdminException("sin conexión", new RuntimeException("x")));

        assertThat(respuesta.getStatusCode().value()).isEqualTo(502);
    }
}