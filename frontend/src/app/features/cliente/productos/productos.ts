import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ProductoService, Producto } from '../../../core/services/producto.service';
import { CarritoService } from '../../../core/services/carrito.service';

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './productos.html'
})
export class Productos implements OnInit {
  private productoService = inject(ProductoService);
  private carritoService = inject(CarritoService);
  private cdr = inject(ChangeDetectorRef);

  productos: Producto[] = [];

  ngOnInit(): void {
    this.productoService.listar().subscribe({
      next: (datos) => {
        this.productos = datos;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error al cargar los productos:', err)
    });
  }

  agregarAlCarrito(producto: Producto): void {
    this.carritoService.agregar(producto);
  }
}