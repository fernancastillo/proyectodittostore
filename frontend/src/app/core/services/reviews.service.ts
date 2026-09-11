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

@Injectable({ providedIn: 'root' })
export class ReviewsService {
  private http = inject(HttpClient);

  private baseUrl = `${environment.apiConfig.gatewayUri}/api/reviews`;

  obtenerPorProducto(productoId: number): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.baseUrl}/producto/${productoId}`);
  }
}