import { Injectable, computed, signal } from '@angular/core';
import { Producto } from './producto.service';

@Injectable({ providedIn: 'root' })
export class CarritoService {
  private items = signal<Producto[]>([]);

  readonly listaItems = this.items.asReadonly();
  readonly cantidad = computed(() => this.items().length);

  agregar(producto: Producto): void {
    this.items.update((actual) => [...actual, producto]);
  }

  quitar(index: number): void {
    this.items.update((actual) => actual.filter((_, i) => i !== index));
  }

  vaciar(): void {
    this.items.set([]);
  }
}