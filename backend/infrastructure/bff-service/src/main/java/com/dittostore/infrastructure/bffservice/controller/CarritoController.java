package com.dittostore.infrastructure.bffservice.controller;

import com.dittostore.infrastructure.bffservice.client.CarritoClient;
import com.dittostore.infrastructure.bffservice.client.ProductoClient;
import com.dittostore.infrastructure.bffservice.client.UsuariosClient;
import com.dittostore.infrastructure.bffservice.dto.AgregarItemCarritoRequestDTO;
import com.dittostore.infrastructure.bffservice.dto.CarritoBffResponseDTO;
import com.dittostore.infrastructure.bffservice.dto.CarritoDTO;
import com.dittostore.infrastructure.bffservice.dto.CarritoItemBffResponseDTO;
import com.dittostore.infrastructure.bffservice.dto.CarritoItemDTO;
import com.dittostore.infrastructure.bffservice.dto.CarritoItemServiceRequestDTO;
import com.dittostore.infrastructure.bffservice.dto.ProductoDTO;
import com.dittostore.infrastructure.bffservice.exception.StockInsuficienteException; 
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bff/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoClient carritoClient;
    private final ProductoClient productoClient;
    private final UsuariosClient usuariosClient;

    @GetMapping
    public CarritoBffResponseDTO obtenerCarrito(@AuthenticationPrincipal Jwt jwt) {
        Long usuarioId = resolverUsuarioId(jwt);
        CarritoDTO carrito = carritoClient.obtenerOCrearActivo(usuarioId);
        return enriquecer(carrito);
    }

    @PostMapping("/items")
    public CarritoBffResponseDTO agregarItem(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AgregarItemCarritoRequestDTO request) {
        Long usuarioId = resolverUsuarioId(jwt);
        CarritoDTO carritoActivo = carritoClient.obtenerOCrearActivo(usuarioId);

        ProductoDTO producto = productoClient.obtenerDetalleProducto(request.getProductoId());

        int cantidadActualEnCarrito = carritoActivo.getItems() == null ? 0
                : carritoActivo.getItems().stream()
                        .filter(i -> i.getProductoId().equals(request.getProductoId()))
                        .mapToInt(CarritoItemDTO::getCantidad)
                        .sum();
        int cantidadTotalSolicitada = cantidadActualEnCarrito + request.getCantidad();

        if (producto.getStock() == null || cantidadTotalSolicitada > producto.getStock()) {
            throw new StockInsuficienteException(producto.getNombre(), producto.getStock(), cantidadTotalSolicitada);
        }

        CarritoItemServiceRequestDTO itemRequest = new CarritoItemServiceRequestDTO(
                producto.getId(),
                request.getCantidad(),
                producto.getPrecio());

        CarritoDTO actualizado = carritoClient.agregarItem(carritoActivo.getId(), itemRequest);
        return enriquecer(actualizado);
    }

    @PatchMapping("/items/{itemId}/incrementar")
    public CarritoBffResponseDTO incrementarItem(@AuthenticationPrincipal Jwt jwt, @PathVariable Long itemId) {
        Long usuarioId = resolverUsuarioId(jwt);
        CarritoDTO carritoActivo = carritoClient.obtenerOCrearActivo(usuarioId);

        List<CarritoItemDTO> itemsActuales = carritoActivo.getItems() != null ? carritoActivo.getItems() : List.of();
        itemsActuales.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> {
                    ProductoDTO producto = productoClient.obtenerDetalleProducto(item.getProductoId());
                    int nuevaCantidad = item.getCantidad() + 1;
                    if (producto.getStock() == null || nuevaCantidad > producto.getStock()) {
                        throw new StockInsuficienteException(producto.getNombre(), producto.getStock(), nuevaCantidad);
                    }
                });

        CarritoDTO actualizado = carritoClient.incrementarItem(carritoActivo.getId(), itemId);
        return enriquecer(actualizado);
    }

    @PatchMapping("/items/{itemId}/decrementar")
    public CarritoBffResponseDTO decrementarItem(@AuthenticationPrincipal Jwt jwt, @PathVariable Long itemId) {
        Long usuarioId = resolverUsuarioId(jwt);
        CarritoDTO carritoActivo = carritoClient.obtenerOCrearActivo(usuarioId);
        CarritoDTO actualizado = carritoClient.decrementarItem(carritoActivo.getId(), itemId);
        return enriquecer(actualizado);
    }

    @DeleteMapping("/items/{itemId}")
    public CarritoBffResponseDTO eliminarItem(@AuthenticationPrincipal Jwt jwt, @PathVariable Long itemId) {
        Long usuarioId = resolverUsuarioId(jwt);
        CarritoDTO carritoActivo = carritoClient.obtenerOCrearActivo(usuarioId);
        CarritoDTO actualizado = carritoClient.eliminarItem(carritoActivo.getId(), itemId);
        return enriquecer(actualizado);
    }

    @DeleteMapping
    public CarritoBffResponseDTO vaciarCarrito(@AuthenticationPrincipal Jwt jwt) {
        Long usuarioId = resolverUsuarioId(jwt);
        CarritoDTO carritoActivo = carritoClient.obtenerOCrearActivo(usuarioId);
        CarritoDTO actualizado = carritoClient.vaciarItems(carritoActivo.getId());
        return enriquecer(actualizado);
    }

    private Long resolverUsuarioId(Jwt jwt) {
        return usuariosClient.obtenerDetalleUsuarioPorAzureId(jwt.getSubject()).getId();
    }

    private CarritoBffResponseDTO enriquecer(CarritoDTO carrito) {
        List<CarritoItemDTO> items = carrito.getItems() != null ? carrito.getItems() : List.of();

        List<CarritoItemBffResponseDTO> itemsEnriquecidos = items.stream()
                .map(item -> {
                    ProductoDTO producto = productoClient.obtenerDetalleProducto(item.getProductoId());
                    return CarritoItemBffResponseDTO.builder()
                            .id(item.getId())
                            .productoId(item.getProductoId())
                            .cantidad(item.getCantidad())
                            .precioUnitario(item.getPrecioUnitario())
                            .subtotal(
                                    item.getPrecioUnitario().multiply(java.math.BigDecimal.valueOf(item.getCantidad())))
                            .nombre(producto.getNombre())
                            .imagenUrl(producto.getImagenUrl())
                            .coleccionSet(producto.getColeccionSet())
                            .stock(producto.getStock()) 
                            .build();
                })
                .toList();

        return CarritoBffResponseDTO.builder()
                .id(carrito.getId())
                .usuarioId(carrito.getUsuarioId())
                .estado(carrito.getEstado())
                .items(itemsEnriquecidos)
                .build();
    }
}