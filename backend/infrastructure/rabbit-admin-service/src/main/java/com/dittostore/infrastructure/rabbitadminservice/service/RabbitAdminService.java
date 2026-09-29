package com.dittostore.infrastructure.rabbitadminservice.service;

import com.dittostore.infrastructure.rabbitadminservice.dto.BindingRequestDTO;
import com.dittostore.infrastructure.rabbitadminservice.dto.ExchangeRequestDTO;
import com.dittostore.infrastructure.rabbitadminservice.dto.QueueRequestDTO;

public interface RabbitAdminService {

    void crearCola(QueueRequestDTO request);

    void eliminarCola(String nombre);

    void crearExchange(ExchangeRequestDTO request);

    void eliminarExchange(String nombre);

    void crearBinding(BindingRequestDTO request);

    void eliminarBinding(BindingRequestDTO request);
}