import { Injectable, signal } from '@angular/core';

export interface PedidoItem {
  productId: string;
  nombre: string;
  imagen: string;
  cantidad: number;
  precioUnitario: number;
}

export type MetodoPago = 'paypal' | 'tarjeta-ditto';

export interface Pedido {
  id: string;
  fecha: string;
  items: PedidoItem[];
  subtotal: number;
  envio: number;
  total: number;
  metodoPago: MetodoPago;
  estado: 'pagado';
}

const STORAGE_KEY = 'ditto-store-pedidos';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly pedidos = signal<Pedido[]>(this.cargarDeStorage());

  readonly pedidos$ = this.pedidos.asReadonly();

  crearPedido(datos: {
    items: PedidoItem[];
    subtotal: number;
    envio: number;
    total: number;
    metodoPago: MetodoPago;
  }): Pedido {
    const pedido: Pedido = {
      id: this.generarId(),
      fecha: new Date().toISOString(),
      estado: 'pagado',
      ...datos,
    };

    this.pedidos.update((actuales) => [pedido, ...actuales]);
    this.guardarEnStorage(this.pedidos());
    return pedido;
  }

  private generarId(): string {
    const codigo = Math.random().toString(36).slice(2, 8).toUpperCase();
    return `DT-${codigo}`;
  }

  private cargarDeStorage(): Pedido[] {
    if (typeof localStorage === 'undefined') return [];
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as Pedido[]) : [];
    } catch {
      return [];
    }
  }

  private guardarEnStorage(pedidos: Pedido[]): void {
    if (typeof localStorage === 'undefined') return;
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(pedidos));
    } catch {
      /* almacenamiento no disponible, se ignora */
    }
  }
}