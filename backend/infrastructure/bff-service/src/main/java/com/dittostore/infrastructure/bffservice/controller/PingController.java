package com.dittostore.infrastructure.bffservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class PingController {

    @GetMapping("/bff/ping")
    public Map<String, Object> ping() {
        return Map.of(
            "status", "ok",
            "service", "bff-service",
            "timestamp", LocalDateTime.now()
        );
    }
}