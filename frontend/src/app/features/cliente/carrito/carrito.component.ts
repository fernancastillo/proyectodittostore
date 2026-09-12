import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CarritoService } from '../../../core/services/carrito.service';
import { Carrito, CheckoutResponse } from './carrito.model';

@Component({
  selector: 'app-carrito',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './carrito.component.html',
  styleUrl: './carrito.component.scss'
})
export class CarritoComponent {
  private carritoService = inject(CarritoService);

  carrito = signal<Carrito | null>(null);
  cargando = signal(false);
  error = signal<string | null>(null);

  direccionEnvio = '';
  pagando = signal(false);
  pedidoConfirmado = signal<CheckoutResponse | null>(null);

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

  incrementar(itemId: number): void {
    this.carritoService.incrementarItem(itemId).subscribe({
      next: (carrito) => this.carrito.set(carrito),
      error: (err) => this.error.set(err.error?.mensaje ?? `Error ${err.status}: ${err.message}`),
    });
  }

  alcanzoStockMaximo(item: { cantidad: number; stock: number }): boolean {
    return item.cantidad >= item.stock;
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

  pagar(): void {
    this.error.set(null);

    if (!this.direccionEnvio.trim()) {
      this.error.set('Ingresa una dirección de envío antes de pagar.');
      return;
    }

    this.pagando.set(true);
    this.carritoService.pagar(this.direccionEnvio, 'TARJETA').subscribe({
      next: (resultado) => {
        this.pedidoConfirmado.set(resultado);
        this.pagando.set(false);
        this.cargarCarrito();
      },
      error: (err) => {
        this.error.set(`Error ${err.status}: ${err.message}`);
        this.pagando.set(false);
      },
    });
  }

  reiniciarProceso(): void {
    this.pedidoConfirmado.set(null);
    this.direccionEnvio = '';
    this.cargarCarrito();
  }
}