package com.dittostore.businessdomain.productoservice.service;

import com.dittostore.businessdomain.productoservice.dto.ProductoRequestDTO;
import com.dittostore.businessdomain.productoservice.dto.ProductoResponseDTO;
import com.dittostore.businessdomain.productoservice.entity.Producto;
import com.dittostore.businessdomain.productoservice.exception.ProductoNotFoundException;
import com.dittostore.businessdomain.productoservice.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    @Override
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        Producto producto = mapearAEntidad(dto);
        Producto guardado = productoRepository.save(producto);
        return mapearAResponse(guardado);
    }

    @Override
    public ProductoResponseDTO obtenerPorId(Long id) {
        Producto producto = buscarOLanzar(id);
        return mapearAResponse(producto);
    }

    @Override
    public List<ProductoResponseDTO> listarTodos() {
        return productoRepository.findAll()
                .stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        Producto producto = buscarOLanzar(id);

        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setColeccionSet(dto.getColeccionSet());
        producto.setTipoCaja(dto.getTipoCaja());
        producto.setCantidadSobres(dto.getCantidadSobres());
        producto.setCartasPorSobre(dto.getCartasPorSobre());
        producto.setIdioma(dto.getIdioma());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setImagenUrl(dto.getImagenUrl());

        Producto actualizado = productoRepository.save(producto);
        return mapearAResponse(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        Producto producto = buscarOLanzar(id);
        productoRepository.delete(producto);
    }

    private Producto buscarOLanzar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));
    }

    private Producto mapearAEntidad(ProductoRequestDTO dto) {
        return Producto.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .coleccionSet(dto.getColeccionSet())
                .tipoCaja(dto.getTipoCaja())
                .cantidadSobres(dto.getCantidadSobres())
                .cartasPorSobre(dto.getCartasPorSobre())
                .idioma(dto.getIdioma())
                .precio(dto.getPrecio())
                .stock(dto.getStock())
                .imagenUrl(dto.getImagenUrl())
                .build();
    }

    private ProductoResponseDTO mapearAResponse(Producto producto) {
        return ProductoResponseDTO.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .coleccionSet(producto.getColeccionSet())
                .tipoCaja(producto.getTipoCaja())
                .cantidadSobres(producto.getCantidadSobres())
                .cartasPorSobre(producto.getCartasPorSobre())
                .idioma(producto.getIdioma())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .imagenUrl(producto.getImagenUrl())
                .build();
    }
}