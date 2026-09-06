package com.dittostore.businessdomain.pagoservice.service;

import com.dittostore.businessdomain.pagoservice.dto.PagoRequestDTO;
import com.dittostore.businessdomain.pagoservice.dto.PagoResponseDTO;
import com.dittostore.businessdomain.pagoservice.entity.EstadoPago;

import java.util.List;

public interface PagoService {

    PagoResponseDTO crear(PagoRequestDTO requestDTO);

    PagoResponseDTO obtenerPorId(Long id);

    List<PagoResponseDTO> obtenerPorPedido(Long pedidoId);

    List<PagoResponseDTO> obtenerTodos();

    PagoResponseDTO actualizarEstado(Long id, EstadoPago nuevoEstado);

    void eliminar(Long id);
}
