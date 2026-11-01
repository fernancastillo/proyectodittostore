package com.dittostore.infrastructure.kafkaadminservice.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidacionKafkaTest {

    @Test
    void nombresValidosDeTopico() {
        assertThat("pedidos.eventos").matches(ValidacionKafka.NOMBRE_REGEX);
        assertThat("pagos.alertas").matches(ValidacionKafka.NOMBRE_REGEX);
        assertThat("topic_1-a").matches(ValidacionKafka.NOMBRE_REGEX);
    }

    @Test
    void nombresInvalidosDeTopico() {
        assertThat("__consumer_offsets").doesNotMatch(ValidacionKafka.NOMBRE_REGEX);
        assertThat(".").doesNotMatch(ValidacionKafka.NOMBRE_REGEX);
        assertThat("..").doesNotMatch(ValidacionKafka.NOMBRE_REGEX);
        assertThat("con espacio").doesNotMatch(ValidacionKafka.NOMBRE_REGEX);
        assertThat("a".repeat(250)).doesNotMatch(ValidacionKafka.NOMBRE_REGEX);
    }

    @Test
    void soloSePermitenLasConfiguracionesAdministrables() {
        assertThat(ValidacionKafka.CONFIGS_PERMITIDAS)
                .contains("retention.ms", "retention.bytes", "cleanup.policy", "min.insync.replicas")
                .doesNotContain("foo");
    }
}