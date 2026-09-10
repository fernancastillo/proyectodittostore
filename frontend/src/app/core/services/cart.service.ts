import { Injectable, computed, effect, signal } from '@angular/core';
import { StoreProduct } from '../models/card.model';

export interface CartItem {
  product: StoreProduct;
  cantidad: number;
}

const STORAGE_KEY = 'ditto-store-carrito';

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly items = signal<CartItem[]>(this.cargarDeStorage());

  /** Lista de items del carrito, solo lectura desde fuera del servicio. */
  readonly items$ = this.items.asReadonly();

  /** Cantidad total de unidades (para el badge del navbar). */
  readonly totalItems = computed(() =>
    this.items().reduce((acc, item) => acc + item.cantidad, 0),
  );

  /** Suma de precio * cantidad de todos los items. */
  readonly subtotal = computed(() =>
    this.items().reduce((acc, item) => acc + item.product.precio * item.cantidad, 0),
  );

  readonly estaVacio = computed(() => this.items().length === 0);

  constructor() {
    effect(() => this.guardarEnStorage(this.items()));
  }

  agregar(product: StoreProduct, cantidad = 1): void {
    this.items.update((actuales) => {
      const idx = actuales.findIndex((i) => i.product.id === product.id);
      if (idx === -1) {
        return [...actuales, { product, cantidad }];
      }
      const copia = [...actuales];
      copia[idx] = { ...copia[idx], cantidad: copia[idx].cantidad + cantidad };
      return copia;
    });
  }

  actualizarCantidad(productId: string, cantidad: number): void {
    if (cantidad < 1) {
      this.eliminar(productId);
      return;
    }
    this.items.update((actuales) =>
      actuales.map((i) => (i.product.id === productId ? { ...i, cantidad } : i)),
    );
  }

  incrementar(productId: string): void {
    this.items.update((actuales) =>
      actuales.map((i) => (i.product.id === productId ? { ...i, cantidad: i.cantidad + 1 } : i)),
    );
  }

  decrementar(productId: string): void {
    const item = this.items().find((i) => i.product.id === productId);
    if (!item) return;
    if (item.cantidad <= 1) {
      this.eliminar(productId);
      return;
    }
    this.items.update((actuales) =>
      actuales.map((i) => (i.product.id === productId ? { ...i, cantidad: i.cantidad - 1 } : i)),
    );
  }

  eliminar(productId: string): void {
    this.items.update((actuales) => actuales.filter((i) => i.product.id !== productId));
  }

  vaciar(): void {
    this.items.set([]);
  }

  private cargarDeStorage(): CartItem[] {
    if (typeof localStorage === 'undefined') return [];
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as CartItem[]) : [];
    } catch {
      return [];
    }
  }

  private guardarEnStorage(items: CartItem[]): void {
    if (typeof localStorage === 'undefined') return;
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
    } catch {
      /* almacenamiento no disponible, se ignora */
    }
  }
}