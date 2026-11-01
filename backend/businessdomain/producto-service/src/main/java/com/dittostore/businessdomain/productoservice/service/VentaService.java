package com.dittostore.businessdomain.productoservice.service;

import com.dittostore.businessdomain.productoservice.dto.VentaProductoResponseDTO;
import com.dittostore.businessdomain.productoservice.messaging.kafka.event.PedidoEventoKafka;

import java.util.List;

public interface VentaService {

    boolean registrarVentaPedido(PedidoEventoKafka evento);

    List<VentaProductoResponseDTO> obtenerVentas();
}