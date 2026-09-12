import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

// Tipos usados por el catálogo y el detalle de producto.
export type TipoCaja = 'BOOSTER_BOX' | 'ELITE_TRAINER_BOX' | 'DISPLAY' | 'FAT_PACK';
export type Idioma = 'ESPANOL' | 'INGLES' | 'JAPONES';

// Interfaz completa del producto (todos los campos que devuelve el backend).
export interface Producto {
  id: number;
  nombre: string;
  descripcion?: string;
  coleccionSet?: string;
  tipoCaja: TipoCaja;
  cantidadSobres?: number;
  cartasPorSobre?: number;
  idioma: Idioma;
  precio: number;
  stock: number;
  imagenUrl?: string;
}

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private http = inject(HttpClient);

  // Apunta al api-gateway (puerto 8080), que enruta directo a
  // producto-service. Es la ruta pública del catálogo, sin login.
  private baseUrl = `${environment.apiConfig.gatewayUri}/api/productos`;

  listar(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.baseUrl);
  }

  obtenerPorId(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.baseUrl}/${id}`);
  }
}