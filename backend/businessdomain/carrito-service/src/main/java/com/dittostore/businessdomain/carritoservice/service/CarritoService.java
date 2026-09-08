package com.dittostore.businessdomain.carritoservice.service;

import com.dittostore.businessdomain.carritoservice.dto.CarritoItemRequestDTO;
import com.dittostore.businessdomain.carritoservice.dto.CarritoRequestDTO;
import com.dittostore.businessdomain.carritoservice.dto.CarritoResponseDTO;
import com.dittostore.businessdomain.carritoservice.entity.EstadoCarrito;

import java.util.List;

public interface CarritoService {

    CarritoResponseDTO crear(CarritoRequestDTO requestDTO);

    CarritoResponseDTO obtenerPorId(Long id);

    List<CarritoResponseDTO> obtenerPorUsuario(Long usuarioId);

    List<CarritoResponseDTO> obtenerTodos();

    CarritoResponseDTO agregarItem(Long carritoId, CarritoItemRequestDTO itemDTO);

    CarritoResponseDTO eliminarItem(Long carritoId, Long itemId);

    CarritoResponseDTO actualizarEstado(Long id, EstadoCarrito nuevoEstado);

    void eliminar(Long id);
}