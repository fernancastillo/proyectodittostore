import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Carrito, CheckoutResponse, MetodoPago } from '../../features/cliente/carrito/carrito.model';

const BASE_URL = `${environment.apiConfig.bffUri}/bff/carrito`;

@Injectable({ providedIn: 'root' })
export class CarritoService {
  private http = inject(HttpClient);


  cantidad = signal<number>(0);

  private actualizarEstado(carrito: Carrito): void {
    if (!carrito || !carrito.items) {
      this.cantidad.set(0);
      return;
    }

    const totalUnidades = carrito.items.reduce((acc, item) => acc + item.cantidad, 0);
    this.cantidad.set(totalUnidades);
  }

  obtenerCarrito(): Observable<Carrito> {
    return this.http.get<Carrito>(BASE_URL).pipe(
      tap((carrito) => this.actualizarEstado(carrito))
    );
  }

  agregarItem(productoId: number, cantidad: number): Observable<Carrito> {
    return this.http.post<Carrito>(`${BASE_URL}/items`, { productoId, cantidad }).pipe(
      tap((carrito) => this.actualizarEstado(carrito))
    );
  }

  incrementarItem(itemId: number): Observable<Carrito> {
    return this.http.patch<Carrito>(`${BASE_URL}/items/${itemId}/incrementar`, {}).pipe(
      tap((carrito) => this.actualizarEstado(carrito))
    );
  }

  decrementarItem(itemId: number): Observable<Carrito> {
    return this.http.patch<Carrito>(`${BASE_URL}/items/${itemId}/decrementar`, {}).pipe(
      tap((carrito) => this.actualizarEstado(carrito))
    );
  }

  eliminarItem(itemId: number): Observable<Carrito> {
    return this.http.delete<Carrito>(`${BASE_URL}/items/${itemId}`).pipe(
      tap((carrito) => this.actualizarEstado(carrito))
    );
  }

  vaciarCarrito(): Observable<Carrito> {
    return this.http.delete<Carrito>(BASE_URL).pipe(
      tap((carrito) => this.actualizarEstado(carrito))
    );
  }

  pagar(direccionEnvio: string, metodoPago: MetodoPago): Observable<CheckoutResponse> {
    return this.http.post<CheckoutResponse>(`${BASE_URL.replace('/carrito', '/pago')}/checkout`, {
      direccionEnvio,
      metodoPago,
    }).pipe(
      tap(() => this.cantidad.set(0))
    );
  }
}