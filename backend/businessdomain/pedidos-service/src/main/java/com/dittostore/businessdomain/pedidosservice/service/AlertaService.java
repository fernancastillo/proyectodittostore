package com.dittostore.businessdomain.pedidosservice.service;

import com.dittostore.businessdomain.pedidosservice.dto.AlertaPedidoResponseDTO;
import com.dittostore.businessdomain.pedidosservice.messaging.kafka.event.AlertaPagoKafkaEvent;

import java.util.List;

public interface AlertaService {

    boolean registrarAlerta(AlertaPagoKafkaEvent evento);

    List<AlertaPedidoResponseDTO> listar();

    List<AlertaPedidoResponseDTO> listarPorPedido(Long pedidoId);
}