package com.dittostore.infrastructure.kafkaadminservice.dto;

import java.util.List;

public record MiembroDTO(String consumerId, String clientId, String host, List<String> particiones) {
}