import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Carrito, CheckoutResponse, MetodoPago } from './carrito.model';

const BASE_URL = `${environment.apiConfig.bffUri}/bff/carrito`;

@Injectable({ providedIn: 'root' })
export class CarritoService {
  private http = inject(HttpClient);

  obtenerCarrito(): Observable<Carrito> {
    return this.http.get<Carrito>(BASE_URL);
  }

  agregarItem(productoId: number, cantidad: number): Observable<Carrito> {
    return this.http.post<Carrito>(`${BASE_URL}/items`, { productoId, cantidad });
  }

  incrementarItem(itemId: number): Observable<Carrito> {
    return this.http.patch<Carrito>(`${BASE_URL}/items/${itemId}/incrementar`, {});
  }

  decrementarItem(itemId: number): Observable<Carrito> {
    return this.http.patch<Carrito>(`${BASE_URL}/items/${itemId}/decrementar`, {});
  }

  eliminarItem(itemId: number): Observable<Carrito> {
    return this.http.delete<Carrito>(`${BASE_URL}/items/${itemId}`);
  }

  vaciarCarrito(): Observable<Carrito> {
    return this.http.delete<Carrito>(BASE_URL);
  }

  pagar(direccionEnvio: string, metodoPago: MetodoPago): Observable<CheckoutResponse> {
  return this.http.post<CheckoutResponse>(`${BASE_URL.replace('/carrito', '/pago')}/checkout`, {
    direccionEnvio,
    metodoPago,
  });
}
}