package com.dittostore.businessdomain.pedidosservice.service;

import com.dittostore.businessdomain.pedidosservice.dto.PedidoItemRequestDTO;
import com.dittostore.businessdomain.pedidosservice.dto.PedidoItemResponseDTO;
import com.dittostore.businessdomain.pedidosservice.dto.PedidoRequestDTO;
import com.dittostore.businessdomain.pedidosservice.dto.PedidoResponseDTO;
import com.dittostore.businessdomain.pedidosservice.entity.EstadoPedido;
import com.dittostore.businessdomain.pedidosservice.entity.Pedido;
import com.dittostore.businessdomain.pedidosservice.entity.PedidoItem;
import com.dittostore.businessdomain.pedidosservice.exception.PedidoNotFoundException;
import com.dittostore.businessdomain.pedidosservice.repository.PedidoItemRepository;
import com.dittostore.businessdomain.pedidosservice.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoItemRepository pedidoItemRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, PedidoItemRepository pedidoItemRepository) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoItemRepository = pedidoItemRepository;
    }

    @Override
    @Transactional
    public PedidoResponseDTO crear(PedidoRequestDTO requestDTO) {
        Pedido pedido = Pedido.builder()
                .usuarioId(requestDTO.getUsuarioId())
                .fechaPedido(LocalDateTime.now())
                .estado(EstadoPedido.PENDIENTE)
                .direccionEnvio(requestDTO.getDireccionEnvio())
                .total(BigDecimal.ZERO)
                .build();
        pedido = pedidoRepository.save(pedido);

        BigDecimal total = BigDecimal.ZERO;
        for (PedidoItemRequestDTO itemDTO : requestDTO.getItems()) {
            BigDecimal subtotal = itemDTO.getPrecioUnitario().multiply(BigDecimal.valueOf(itemDTO.getCantidad()));
            PedidoItem item = PedidoItem.builder()
                    .pedidoId(pedido.getId())
                    .productoId(itemDTO.getProductoId())
                    .cantidad(itemDTO.getCantidad())
                    .precioUnitario(itemDTO.getPrecioUnitario())
                    .subtotal(subtotal)
                    .build();
            pedidoItemRepository.save(item);
            total = total.add(subtotal);
        }

        pedido.setTotal(total);
        pedido = pedidoRepository.save(pedido);

        return toResponseDTO(pedido);
    }

    @Override
    public PedidoResponseDTO obtenerPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException(id));
        return toResponseDTO(pedido);
    }

    @Override
    public List<PedidoResponseDTO> obtenerPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public List<PedidoResponseDTO> obtenerTodos() {
        return pedidoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public PedidoResponseDTO actualizarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException(id));
        pedido.setEstado(nuevoEstado);
        pedido = pedidoRepository.save(pedido);
        return toResponseDTO(pedido);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new PedidoNotFoundException(id);
        }
        pedidoItemRepository.deleteByPedidoId(id);
        pedidoRepository.deleteById(id);
    }

    private PedidoResponseDTO toResponseDTO(Pedido pedido) {
        List<PedidoItemResponseDTO> items = pedidoItemRepository.findByPedidoId(pedido.getId()).stream()
                .map(item -> PedidoItemResponseDTO.builder()
                        .id(item.getId())
                        .productoId(item.getProductoId())
                        .cantidad(item.getCantidad())
                        .precioUnitario(item.getPrecioUnitario())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return PedidoResponseDTO.builder()
                .id(pedido.getId())
                .usuarioId(pedido.getUsuarioId())
                .fechaPedido(pedido.getFechaPedido())
                .estado(pedido.getEstado())
                .direccionEnvio(pedido.getDireccionEnvio())
                .total(pedido.getTotal())
                .items(items)
                .build();
    }
}
