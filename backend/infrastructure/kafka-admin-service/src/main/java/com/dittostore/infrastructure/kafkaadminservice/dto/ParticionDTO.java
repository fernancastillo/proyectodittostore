package com.dittostore.infrastructure.kafkaadminservice.dto;

import java.util.List;

public record ParticionDTO(int particion, int lider, List<Integer> replicas, List<Integer> isr) {
}