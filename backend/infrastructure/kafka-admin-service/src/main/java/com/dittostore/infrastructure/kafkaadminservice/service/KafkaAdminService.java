package com.dittostore.infrastructure.kafkaadminservice.service;

import com.dittostore.infrastructure.kafkaadminservice.dto.GrupoDetalleDTO;
import com.dittostore.infrastructure.kafkaadminservice.dto.TopicDetalleDTO;
import com.dittostore.infrastructure.kafkaadminservice.dto.TopicRequestDTO;

import java.util.List;
import java.util.Map;

public interface KafkaAdminService {

    void crearTopic(TopicRequestDTO request);

    List<String> listarTopics();

    TopicDetalleDTO describirTopic(String nombre);

    void modificarConfiguracion(String nombre, Map<String, String> configuraciones);

    void aumentarParticiones(String nombre, int particiones);

    void eliminarTopic(String nombre);

    List<String> listarGrupos();

    GrupoDetalleDTO describirGrupo(String groupId);
}