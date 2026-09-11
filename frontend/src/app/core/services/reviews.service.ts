import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Review {
  id: number;
  productoId: number;
  usuarioId: number;
  calificacion: number;
  comentario: string;
}

export interface ReviewRequest {
  productoId: number;
  usuarioId: number;
  calificacion: number;
  comentario: string;
}

@Injectable({ providedIn: 'root' })
export class ReviewsService {
  private http = inject(HttpClient);

  private baseUrl = `${environment.apiConfig.gatewayUri}/api/reviews`;

  obtenerPorProducto(productoId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.baseUrl}/producto/${productoId}`);
  }

  obtenerPorUsuario(usuarioId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.baseUrl}/usuario/${usuarioId}`);
  }

  crear(dto: ReviewRequest): Observable<Review> {
    return this.http.post<Review>(this.baseUrl, dto);
  }

  actualizar(id: number, dto: ReviewRequest): Observable<Review> {
    return this.http.put<Review>(`${this.baseUrl}/${id}`, dto);
  }
}