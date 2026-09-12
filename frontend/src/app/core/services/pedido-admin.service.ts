import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type EstadoPedido = 'PENDIENTE' | 'CONFIRMADO' | 'ENVIADO' | 'ENTREGADO' | 'CANCELADO';

export interface PedidoItem {
  id: number;
  productoId: number;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface Pedido {
  id: number;
  usuarioId: number;
  fechaPedido: string;
  estado: EstadoPedido;
  direccionEnvio: string;
  total: number;
  items: PedidoItem[];
}

@Injectable({ providedIn: 'root' })
export class PedidoAdminService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiConfig.bffUri}/bff/admin/pedidos`;

  obtenerTodos(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(this.baseUrl);
  }

  actualizarEstado(id: number, estado: EstadoPedido): Observable<Pedido> {
    return this.http.patch<Pedido>(`${this.baseUrl}/${id}/estado`, { estado });
  }
}