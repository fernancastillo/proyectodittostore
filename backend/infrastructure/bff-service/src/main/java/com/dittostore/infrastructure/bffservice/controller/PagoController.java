package com.dittostore.infrastructure.bffservice.controller;

import com.dittostore.infrastructure.bffservice.client.CarritoClient;
import com.dittostore.infrastructure.bffservice.client.PagoClient;
import com.dittostore.infrastructure.bffservice.client.PedidosClient;
import com.dittostore.infrastructure.bffservice.client.ProductoClient;
import com.dittostore.infrastructure.bffservice.client.UsuariosClient;
import com.dittostore.infrastructure.bffservice.dto.*;
import com.dittostore.infrastructure.bffservice.exception.CarritoVacioException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bff/pago")
@RequiredArgsConstructor 
public class PagoController {

    private final CarritoClient carritoClient;
    private final UsuariosClient usuariosClient;
    private final PedidosClient pedidosClient;
    private final PagoClient pagoClient;
    private final ProductoClient productoClient; 

    @PostMapping("/checkout")
    public CheckoutResponseDTO pagar(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CheckoutRequestDTO request) {
        Long usuarioId = resolverUsuarioId(jwt);

        CarritoDTO carritoActivo = carritoClient.obtenerOCrearActivo(usuarioId);
        List<CarritoItemDTO> items = carritoActivo.getItems();

        if (items == null || items.isEmpty()) {
            throw new CarritoVacioException();
        }

        List<ReducirStockItemDTO> itemsStock = items.stream()
                .map(item -> new ReducirStockItemDTO(item.getProductoId(), item.getCantidad()))
                .toList();
        productoClient.reducirStock(new ReducirStockRequestDTO(itemsStock));

        List<PedidoItemRequestDTO> itemsPedido = items.stream()
                .map(item -> new PedidoItemRequestDTO(item.getProductoId(), item.getCantidad(),
                        item.getPrecioUnitario()))
                .toList();

        PedidoRequestDTO pedidoRequest = new PedidoRequestDTO(usuarioId, request.getDireccionEnvio(), itemsPedido);
        PedidoResponseDTO pedido = pedidosClient.crearPedido(pedidoRequest);

        PagoRequestDTO pagoRequest = new PagoRequestDTO(pedido.getId(), pedido.getTotal(),
                request.getMetodoPago().name());
        PagoResponseDTO pago = pagoClient.crearPago(pagoRequest);

        pago = pagoClient.actualizarEstadoPago(pago.getId(), new EstadoPagoUpdateRequestDTO("APROBADO"));
        pedido = pedidosClient.actualizarEstadoPedido(pedido.getId(), new EstadoUpdateRequestDTO("CONFIRMADO"));

        carritoClient.vaciarItems(carritoActivo.getId());

        return CheckoutResponseDTO.builder()
                .pedidoId(pedido.getId())
                .estadoPedido(pedido.getEstado())
                .total(pedido.getTotal())
                .pagoId(pago.getId())
                .estadoPago(pago.getEstado())
                .transaccionId(pago.getTransaccionId())
                .metodoPago(pago.getMetodoPago())
                .build();
    }

    private Long resolverUsuarioId(Jwt jwt) {
        return usuariosClient.obtenerDetalleUsuarioPorAzureId(jwt.getSubject()).getId();
    }
}