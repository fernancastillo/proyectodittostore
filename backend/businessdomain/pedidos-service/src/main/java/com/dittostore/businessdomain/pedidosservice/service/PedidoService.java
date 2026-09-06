package com.dittostore.businessdomain.pedidosservice.service;

import com.dittostore.businessdomain.pedidosservice.dto.PedidoRequestDTO;
import com.dittostore.businessdomain.pedidosservice.dto.PedidoResponseDTO;
import com.dittostore.businessdomain.pedidosservice.entity.EstadoPedido;

import java.util.List;

public interface PedidoService {

    PedidoResponseDTO crear(PedidoRequestDTO requestDTO);

    PedidoResponseDTO obtenerPorId(Long id);

    List<PedidoResponseDTO> obtenerPorUsuario(Long usuarioId);

    List<PedidoResponseDTO> obtenerTodos();

    PedidoResponseDTO actualizarEstado(Long id, EstadoPedido nuevoEstado);

    void eliminar(Long id);
}
