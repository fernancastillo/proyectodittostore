import { Component, inject } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { CarritoService } from '../../../core/services/carrito.service';

@Component({
  selector: 'app-carrito',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './carrito.html'
})
export class Carrito {
  carritoService = inject(CarritoService);

  quitar(index: number): void {
    this.carritoService.quitar(index);
  }

  vaciar(): void {
    this.carritoService.vaciar();
  }

  get total(): number {
    return this.carritoService.listaItems().reduce((acc, p) => acc + p.precio, 0);
  }
}