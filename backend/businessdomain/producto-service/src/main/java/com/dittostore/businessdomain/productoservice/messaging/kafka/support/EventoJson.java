package com.dittostore.businessdomain.productoservice.messaging.kafka.support;

import org.springframework.stereotype.Component;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Component
public class EventoJson {

    private final JsonMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public String aJson(Object evento) {
        return mapper.writeValueAsString(evento);
    }

    public <T> T desdeJson(String json, Class<T> tipo) {
        return mapper.readValue(json, tipo);
    }
}