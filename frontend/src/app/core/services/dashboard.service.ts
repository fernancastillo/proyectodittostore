import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { forkJoin, map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Pedido {
  id: number;
  estado: string;
  total: number;
}

export interface DashboardMetricas {
  totalProductos: number;
  totalUsuarios: number;
  totalPedidos: number;
  ingresosTotales: number;
  pedidosPorEstado: Record<string, number>;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiConfig.bffUri;

  obtenerMetricas(): Observable<DashboardMetricas> {
    return forkJoin({
      productos: this.http.get<any[]>(`${this.baseUrl}/bff/admin/productos`),
      usuarios: this.http.get<any[]>(`${this.baseUrl}/bff/admin/usuarios`),
      pedidos: this.http.get<Pedido[]>(`${this.baseUrl}/bff/admin/pedidos`)
    }).pipe(
      map(({ productos, usuarios, pedidos }) => {
        const pedidosPorEstado: Record<string, number> = {};
        let ingresosTotales = 0;

        for (const pedido of pedidos) {
          pedidosPorEstado[pedido.estado] = (pedidosPorEstado[pedido.estado] ?? 0) + 1;
          if (pedido.estado !== 'CANCELADO') {
            ingresosTotales += Number(pedido.total);
          }
        }

        return {
          totalProductos: productos.length,
          totalUsuarios: usuarios.length,
          totalPedidos: pedidos.length,
          ingresosTotales,
          pedidosPorEstado
        };
      })
    );
  }
}