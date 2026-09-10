import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductoService, Producto } from '../core/services/producto.service';
import { CarritoService } from '../core/services/carrito.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [DecimalPipe, RouterLink],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home implements OnInit {
  private productoService = inject(ProductoService);
  private carritoService = inject(CarritoService);
  private cdr = inject(ChangeDetectorRef);

  productosDestacados: Producto[] = [];

  ngOnInit(): void {
    this.cargarProductos();
  }

  private cargarProductos(): void {
    this.productoService.listar().subscribe({
      next: (datos) => {
        this.productosDestacados = datos;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar los productos:', err);
      }
    });
  }

  agregarAlCarrito(producto: Producto): void {
    this.carritoService.agregar(producto);
  }
}