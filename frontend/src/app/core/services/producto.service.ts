import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Producto {
  id: number;
  nombre: string;
  precio: number;
  imagenUrl: string;
}

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private http = inject(HttpClient);

  listar(): Observable<Producto[]> {
    return this.http.get<Producto[]>(`${environment.apiConfig.gatewayUri}/api/productos`);
  }
}