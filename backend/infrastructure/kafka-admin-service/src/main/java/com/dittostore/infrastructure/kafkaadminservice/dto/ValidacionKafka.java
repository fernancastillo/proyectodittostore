package com.dittostore.infrastructure.kafkaadminservice.dto;

import java.util.Set;

public final class ValidacionKafka {

    public static final String NOMBRE_REGEX = "^(?!\\.{1,2}$)(?!__)[A-Za-z0-9._-]{1,249}$";
    public static final String NOMBRE_MENSAJE =
            "Nombre inválido: solo letras, números, '.', '_' o '-' (máx. 249), no puede ser '.' ni '..' ni comenzar con '__'";

    public static final String GRUPO_REGEX = "^[A-Za-z0-9._-]{1,255}$";
    public static final String GRUPO_MENSAJE =
            "Id de grupo inválido: solo letras, números, '.', '_' o '-' (máx. 255)";

    public static final Set<String> CONFIGS_PERMITIDAS = Set.of(
            "retention.ms",
            "retention.bytes",
            "cleanup.policy",
            "min.insync.replicas",
            "segment.bytes",
            "segment.ms",
            "max.message.bytes",
            "compression.type",
            "delete.retention.ms");

    private ValidacionKafka() {
    }
}