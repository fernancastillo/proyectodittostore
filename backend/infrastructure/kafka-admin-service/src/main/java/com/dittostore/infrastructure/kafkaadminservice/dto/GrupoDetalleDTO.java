package com.dittostore.infrastructure.kafkaadminservice.dto;

import java.util.List;

public record GrupoDetalleDTO(
        String groupId,
        int totalMiembros,
        List<MiembroDTO> miembros,
        List<OffsetGrupoDTO> offsets,
        long lagTotal) {
}