package com.dittostore.infrastructure.kafkaadminservice.dto;

public record OffsetGrupoDTO(String topic, int particion, long offsetActual, long offsetFinal, long lag) {
}