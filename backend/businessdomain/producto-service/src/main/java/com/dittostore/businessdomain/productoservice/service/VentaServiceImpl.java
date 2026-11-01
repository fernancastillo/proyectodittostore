package com.dittostore.businessdomain.productoservice.service;

import com.dittostore.businessdomain.productoservice.dto.VentaProductoResponseDTO;
import com.dittostore.businessdomain.productoservice.entity.PedidoVentaProcesado;
import com.dittostore.businessdomain.productoservice.entity.Producto;
import com.dittostore.businessdomain.productoservice.entity.ProductoVenta;
import com.dittostore.businessdomain.productoservice.messaging.kafka.event.PedidoEventoKafka;
import com.dittostore.businessdomain.productoservice.repository.PedidoVentaProcesadoRepository;
import com.dittostore.businessdomain.productoservice.repository.ProductoRepository;
import com.dittostore.businessdomain.productoservice.repository.ProductoVentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VentaServiceImpl implements VentaService {

    private final ProductoVentaRepository ventaRepository;
    private final PedidoVentaProcesadoRepository procesadoRepository;
    private final ProductoRepository productoRepository;

    public VentaServiceImpl(ProductoVentaRepository ventaRepository,
                            PedidoVentaProcesadoRepository procesadoRepository,
                            ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.procesadoRepository = procesadoRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional
    public boolean registrarVentaPedido(PedidoEventoKafka evento) {
        if (procesadoRepository.existsById(evento.pedidoId())) {
            return false;
        }
        LocalDateTime ahora = LocalDateTime.now();

        List<PedidoEventoKafka.Item> items = evento.items().stream()
                .sorted(Comparator.comparing(PedidoEventoKafka.Item::productoId))
                .toList();

        for (PedidoEventoKafka.Item item : items) {
            ProductoVenta venta = ventaRepository.buscarParaActualizar(item.productoId())
                    .orElseGet(() -> ProductoVenta.builder()
                            .productoId(item.productoId())
                            .unidadesVendidas(0L)
                            .montoTotal(BigDecimal.ZERO)
                            .build());
            venta.setUnidadesVendidas(venta.getUnidadesVendidas() + item.cantidad());
            venta.setMontoTotal(venta.getMontoTotal().add(item.subtotal()));
            venta.setUltimaActualizacion(ahora);
            ventaRepository.save(venta);
        }

        procesadoRepository.save(new PedidoVentaProcesado(evento.pedidoId(), ahora));
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaProductoResponseDTO> obtenerVentas() {
        Map<Long, String> nombres = productoRepository.findAll().stream()
                .collect(Collectors.toMap(Producto::getId, Producto::getNombre));

        return ventaRepository.findAllByOrderByUnidadesVendidasDesc().stream()
                .map(v -> new VentaProductoResponseDTO(
                        v.getProductoId(),
                        nombres.getOrDefault(v.getProductoId(), "(producto no encontrado)"),
                        v.getUnidadesVendidas(),
                        v.getMontoTotal(),
                        v.getUltimaActualizacion()))
                .toList();
    }
}