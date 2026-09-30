package com.dittostore.businessdomain.reviewsservice.service;

import com.dittostore.businessdomain.reviewsservice.entity.CompraVerificada;
import com.dittostore.businessdomain.reviewsservice.exception.PedidoNoRegistradoException;
import com.dittostore.businessdomain.reviewsservice.repository.CompraVerificadaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompraVerificadaServiceImpl implements CompraVerificadaService {

    private final CompraVerificadaRepository compraVerificadaRepository;

    @Override
    @Transactional
    public void registrarCompra(Long pedidoId, Long usuarioId, Long productoId, Integer cantidad) {
        if (compraVerificadaRepository.existsByPedidoIdAndProductoId(pedidoId, productoId)) {
            return;
        }
        compraVerificadaRepository.save(CompraVerificada.builder()
            .pedidoId(pedidoId)
            .usuarioId(usuarioId)
            .productoId(productoId)
            .cantidad(cantidad)
            .pagado(false)
            .build());
    }

    @Override
    @Transactional
    public void marcarPagado(Long pedidoId) {
        List<CompraVerificada> compras = compraVerificadaRepository.findByPedidoId(pedidoId);
        if (compras.isEmpty()) {
            throw new PedidoNoRegistradoException(pedidoId);
        }
        compras.forEach(compra -> compra.setPagado(true));
        compraVerificadaRepository.saveAll(compras);
    }
}
