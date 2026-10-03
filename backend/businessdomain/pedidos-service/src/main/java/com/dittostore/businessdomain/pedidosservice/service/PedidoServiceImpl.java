package com.dittostore.businessdomain.pedidosservice.service;

import com.dittostore.businessdomain.pedidosservice.dto.PedidoItemRequestDTO;
import com.dittostore.businessdomain.pedidosservice.dto.PedidoItemResponseDTO;
import com.dittostore.businessdomain.pedidosservice.dto.PedidoRequestDTO;
import com.dittostore.businessdomain.pedidosservice.dto.PedidoResponseDTO;
import com.dittostore.businessdomain.pedidosservice.entity.EstadoPedido;
import com.dittostore.businessdomain.pedidosservice.entity.Pedido;
import com.dittostore.businessdomain.pedidosservice.entity.PedidoItem;
import com.dittostore.businessdomain.pedidosservice.exception.PedidoNotFoundException;
import com.dittostore.businessdomain.pedidosservice.messaging.producer.PedidoProducer;
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
    private final PedidoProducer pedidoProducer;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, PedidoItemRepository pedidoItemRepository,
                              PedidoProducer pedidoProducer) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoItemRepository = pedidoItemRepository;
        this.pedidoProducer = pedidoProducer;
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

        pedidoProducer.publicarCambioEstado(pedido.getId(), pedido.getUsuarioId(), pedido.getEstado());
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
        pedido = cambiarEstado(pedido, nuevoEstado);
        return toResponseDTO(pedido);
    }

    @Override
    @Transactional
    public void aplicarResultadoPago(Long pedidoId, String estadoPago) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new PedidoNotFoundException(pedidoId));

        String estado = estadoPago == null ? "" : estadoPago.toUpperCase();
        if ("APROBADO".equals(estado)) {
            if (pedido.getEstado() == EstadoPedido.PENDIENTE) {
                cambiarEstado(pedido, EstadoPedido.CONFIRMADO);
            }
        } else if ("REEMBOLSADO".equals(estado)) {
            if (pedido.getEstado() != EstadoPedido.ENTREGADO && pedido.getEstado() != EstadoPedido.CANCELADO) {
                cambiarEstado(pedido, EstadoPedido.CANCELADO);
            }
        }
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

    // Guarda el nuevo estado y publica el evento solo si el estado realmente cambió
    private Pedido cambiarEstado(Pedido pedido, EstadoPedido nuevoEstado) {
        EstadoPedido anterior = pedido.getEstado();
        pedido.setEstado(nuevoEstado);
        Pedido guardado = pedidoRepository.save(pedido);
        if (anterior != nuevoEstado) {
            pedidoProducer.publicarCambioEstado(guardado.getId(), guardado.getUsuarioId(), nuevoEstado);
        }
        return guardado;
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