package com.dittostore.businessdomain.pagoservice.service;

import com.dittostore.businessdomain.pagoservice.dto.PagoRequestDTO;
import com.dittostore.businessdomain.pagoservice.dto.PagoResponseDTO;
import com.dittostore.businessdomain.pagoservice.entity.EstadoPago;
import com.dittostore.businessdomain.pagoservice.entity.Pago;
import com.dittostore.businessdomain.pagoservice.exception.PagoNotFoundException;
import com.dittostore.businessdomain.pagoservice.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;

    public PagoServiceImpl(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    @Override
    @Transactional
    public PagoResponseDTO crear(PagoRequestDTO requestDTO) {
        Pago pago = Pago.builder()
                .pedidoId(requestDTO.getPedidoId())
                .monto(requestDTO.getMonto())
                .metodoPago(requestDTO.getMetodoPago())
                .estado(EstadoPago.PENDIENTE)
                // Simula el ID que devolvería una pasarela real (Transbank, Stripe, etc.)
                .transaccionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .fechaPago(LocalDateTime.now())
                .build();

        pago = pagoRepository.save(pago);
        return toResponseDTO(pago);
    }

    @Override
    public PagoResponseDTO obtenerPorId(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new PagoNotFoundException(id));
        return toResponseDTO(pago);
    }

    @Override
    public List<PagoResponseDTO> obtenerPorPedido(Long pedidoId) {
        return pagoRepository.findByPedidoId(pedidoId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public List<PagoResponseDTO> obtenerTodos() {
        return pagoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public PagoResponseDTO actualizarEstado(Long id, EstadoPago nuevoEstado) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new PagoNotFoundException(id));
        pago.setEstado(nuevoEstado);
        pago = pagoRepository.save(pago);
        return toResponseDTO(pago);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!pagoRepository.existsById(id)) {
            throw new PagoNotFoundException(id);
        }
        pagoRepository.deleteById(id);
    }

    private PagoResponseDTO toResponseDTO(Pago pago) {
        return PagoResponseDTO.builder()
                .id(pago.getId())
                .pedidoId(pago.getPedidoId())
                .monto(pago.getMonto())
                .metodoPago(pago.getMetodoPago())
                .estado(pago.getEstado())
                .transaccionId(pago.getTransaccionId())
                .fechaPago(pago.getFechaPago())
                .build();
    }
}
