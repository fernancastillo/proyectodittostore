package com.dittostore.businessdomain.pedidosservice.service;

import com.dittostore.businessdomain.pedidosservice.dto.AlertaPedidoResponseDTO;
import com.dittostore.businessdomain.pedidosservice.entity.PedidoAlerta;
import com.dittostore.businessdomain.pedidosservice.messaging.kafka.event.AlertaPagoKafkaEvent;
import com.dittostore.businessdomain.pedidosservice.repository.PedidoAlertaRepository;
import com.dittostore.businessdomain.pedidosservice.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertaServiceImpl implements AlertaService {

    private static final Logger log = LoggerFactory.getLogger(AlertaServiceImpl.class);

    private final PedidoAlertaRepository alertaRepository;
    private final PedidoRepository pedidoRepository;

    public AlertaServiceImpl(PedidoAlertaRepository alertaRepository, PedidoRepository pedidoRepository) {
        this.alertaRepository = alertaRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    @Transactional
    public boolean registrarAlerta(AlertaPagoKafkaEvent evento) {
        if (!pedidoRepository.existsById(evento.pedidoId())) {
            log.warn("[PEDIDOS-ALERTAS] El pedido {} no existe; se descarta la alerta del pago {}",
                    evento.pedidoId(), evento.pagoId());
            return false;
        }
        if (alertaRepository.existsByPagoIdAndMotivo(evento.pagoId(), evento.motivo())) {
            log.info("[PEDIDOS-ALERTAS] La alerta del pago {} ya estaba registrada; se ignora", evento.pagoId());
            return false;
        }

        alertaRepository.save(PedidoAlerta.builder()
                .pedidoId(evento.pedidoId())
                .pagoId(evento.pagoId())
                .monto(evento.monto())
                .motivo(evento.motivo())
                .severidad(evento.severidad())
                .fechaAlerta(evento.fechaEvento() != null ? evento.fechaEvento() : LocalDateTime.now())
                .build());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaPedidoResponseDTO> listar() {
        return alertaRepository.findAllByOrderByFechaAlertaDesc().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaPedidoResponseDTO> listarPorPedido(Long pedidoId) {
        return alertaRepository.findByPedidoIdOrderByFechaAlertaDesc(pedidoId).stream().map(this::toDTO).toList();
    }

    private AlertaPedidoResponseDTO toDTO(PedidoAlerta a) {
        return new AlertaPedidoResponseDTO(a.getId(), a.getPedidoId(), a.getPagoId(), a.getMonto(),
                a.getMotivo(), a.getSeveridad(), a.getFechaAlerta());
    }
}