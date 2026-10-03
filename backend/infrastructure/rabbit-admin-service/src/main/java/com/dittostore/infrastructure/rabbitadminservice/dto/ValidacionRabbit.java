package com.dittostore.infrastructure.rabbitadminservice.dto;

public final class ValidacionRabbit {

    public static final String NOMBRE_REGEX = "^(?!amq\\.)[A-Za-z0-9._-]{1,255}$";
    public static final String NOMBRE_MENSAJE =
            "Nombre inválido: solo letras, números, '.', '_' o '-' (máx. 255) y no puede comenzar con 'amq.'";

    private ValidacionRabbit() {
    }
}