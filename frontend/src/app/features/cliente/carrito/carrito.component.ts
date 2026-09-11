import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CarritoService } from './carrito.service';
import { Carrito } from './carrito.model';

@Component({
  selector: 'app-carrito',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './carrito.component.html',
})
export class CarritoComponent {
  private carritoService = inject(CarritoService);

  carrito = signal<Carrito | null>(null);
  cargando = signal(false);
  error = signal<string | null>(null);

  // Solo para pruebas mientras no hay catálogo conectado a esta página.
  productoIdPrueba = 1;
  cantidadPrueba = 1;

  constructor() {
    this.cargarCarrito();
  }

  cargarCarrito(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.carritoService.obtenerCarrito().subscribe({
      next: (carrito) => {
        this.carrito.set(carrito);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(`Error ${err.status}: ${err.message}`);
        this.cargando.set(false);
      },
    });
  }

  agregarProductoPrueba(): void {
    this.error.set(null);
    this.carritoService.agregarItem(this.productoIdPrueba, this.cantidadPrueba).subscribe({
      next: (carrito) => this.carrito.set(carrito),
      error: (err) => this.error.set(`Error ${err.status}: ${err.message}`),
    });
  }

  incrementar(itemId: number): void {
    this.carritoService.incrementarItem(itemId).subscribe({
      next: (carrito) => this.carrito.set(carrito),
      error: (err) => this.error.set(`Error ${err.status}: ${err.message}`),
    });
  }

  decrementar(itemId: number): void {
    this.carritoService.decrementarItem(itemId).subscribe({
      next: (carrito) => this.carrito.set(carrito),
      error: (err) => this.error.set(`Error ${err.status}: ${err.message}`),
    });
  }

  eliminar(itemId: number): void {
    this.carritoService.eliminarItem(itemId).subscribe({
      next: (carrito) => this.carrito.set(carrito),
      error: (err) => this.error.set(`Error ${err.status}: ${err.message}`),
    });
  }

  vaciar(): void {
    this.carritoService.vaciarCarrito().subscribe({
      next: (carrito) => this.carrito.set(carrito),
      error: (err) => this.error.set(`Error ${err.status}: ${err.message}`),
    });
  }

  totalCarrito(): number {
    const items = this.carrito()?.items ?? [];
    return items.reduce((acc, item) => acc + item.subtotal, 0);
  }
}