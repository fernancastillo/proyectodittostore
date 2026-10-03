package com.dittostore.businessdomain.pagoservice.messaging.event;

import java.time.LocalDateTime;

public record ReembolsoSolicitadoMessage(Long pagoId, String motivo, LocalDateTime fechaSolicitud) {
}