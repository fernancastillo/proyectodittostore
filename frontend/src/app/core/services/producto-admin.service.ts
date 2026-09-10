import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type TipoCaja = 'BOOSTER_BOX' | 'ELITE_TRAINER_BOX' | 'DISPLAY' | 'FAT_PACK';
export type Idioma = 'ESPANOL' | 'INGLES' | 'JAPONES';

export interface Producto {
  id?: number;
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
export class ProductoAdminService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiConfig.bffUri}/bff/admin/productos`;

  obtenerTodos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.baseUrl);
  }

  crear(producto: Producto): Observable<Producto> {
    return this.http.post<Producto>(this.baseUrl, producto);
  }

  actualizar(id: number, producto: Producto): Observable<Producto> {
    return this.http.put<Producto>(`${this.baseUrl}/${id}`, producto);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}