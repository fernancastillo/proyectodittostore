package com.dittostore.businessdomain.carritoservice.service;

import com.dittostore.businessdomain.carritoservice.dto.CarritoItemRequestDTO;
import com.dittostore.businessdomain.carritoservice.dto.CarritoItemResponseDTO;
import com.dittostore.businessdomain.carritoservice.dto.CarritoRequestDTO;
import com.dittostore.businessdomain.carritoservice.dto.CarritoResponseDTO;
import com.dittostore.businessdomain.carritoservice.entity.Carrito;
import com.dittostore.businessdomain.carritoservice.entity.CarritoItem;
import com.dittostore.businessdomain.carritoservice.entity.EstadoCarrito;
import com.dittostore.businessdomain.carritoservice.exception.CarritoItemNotFoundException;
import com.dittostore.businessdomain.carritoservice.exception.CarritoNotFoundException;
import com.dittostore.businessdomain.carritoservice.repository.CarritoItemRepository;
import com.dittostore.businessdomain.carritoservice.repository.CarritoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CarritoServiceImpl implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository carritoItemRepository;

    public CarritoServiceImpl(CarritoRepository carritoRepository, CarritoItemRepository carritoItemRepository) {
        this.carritoRepository = carritoRepository;
        this.carritoItemRepository = carritoItemRepository;
    }

    @Override
    @Transactional
    public CarritoResponseDTO crear(CarritoRequestDTO requestDTO) {
        Carrito carrito = Carrito.builder()
                .usuarioId(requestDTO.getUsuarioId())
                .estado(EstadoCarrito.ACTIVO)
                .fechaCreacion(LocalDateTime.now())
                .build();
        carrito = carritoRepository.save(carrito);
        return toResponseDTO(carrito);
    }

    @Override
    public CarritoResponseDTO obtenerPorId(Long id) {
        Carrito carrito = carritoRepository.findById(id)
                .orElseThrow(() -> new CarritoNotFoundException(id));
        return toResponseDTO(carrito);
    }

    @Override
    public List<CarritoResponseDTO> obtenerPorUsuario(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public List<CarritoResponseDTO> obtenerTodos() {
        return carritoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public CarritoResponseDTO agregarItem(Long carritoId, CarritoItemRequestDTO itemDTO) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new CarritoNotFoundException(carritoId));

        CarritoItem item = CarritoItem.builder()
                .carritoId(carrito.getId())
                .productoId(itemDTO.getProductoId())
                .cantidad(itemDTO.getCantidad())
                .precioUnitario(itemDTO.getPrecioUnitario())
                .build();
        carritoItemRepository.save(item);

        return toResponseDTO(carrito);
    }

    @Override
    @Transactional
    public CarritoResponseDTO eliminarItem(Long carritoId, Long itemId) {
        Carrito carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() -> new CarritoNotFoundException(carritoId));

        CarritoItem item = carritoItemRepository.findById(itemId)
                .orElseThrow(() -> new CarritoItemNotFoundException(itemId));

        carritoItemRepository.delete(item);

        return toResponseDTO(carrito);
    }

    @Override
    @Transactional
    public CarritoResponseDTO actualizarEstado(Long id, EstadoCarrito nuevoEstado) {
        Carrito carrito = carritoRepository.findById(id)
                .orElseThrow(() -> new CarritoNotFoundException(id));
        carrito.setEstado(nuevoEstado);
        carrito = carritoRepository.save(carrito);
        return toResponseDTO(carrito);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!carritoRepository.existsById(id)) {
            throw new CarritoNotFoundException(id);
        }
        carritoItemRepository.deleteByCarritoId(id);
        carritoRepository.deleteById(id);
    }

    private CarritoResponseDTO toResponseDTO(Carrito carrito) {
        List<CarritoItemResponseDTO> items = carritoItemRepository.findByCarritoId(carrito.getId()).stream()
                .map(item -> CarritoItemResponseDTO.builder()
                        .id(item.getId())
                        .productoId(item.getProductoId())
                        .cantidad(item.getCantidad())
                        .precioUnitario(item.getPrecioUnitario())
                        .build())
                .toList();

        return CarritoResponseDTO.builder()
                .id(carrito.getId())
                .usuarioId(carrito.getUsuarioId())
                .estado(carrito.getEstado())
                .fechaCreacion(carrito.getFechaCreacion())
                .items(items)
                .build();
    }
}