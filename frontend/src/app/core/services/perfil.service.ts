import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, shareReplay } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Perfil {
  id: number;
  azureAdObjectId: string;
  nombre: string;
  apellido: string;
  email: string;
  telefono: string | null;
  direccion: string | null;
  rol: 'CLIENTE' | 'ADMIN';
}

export interface PerfilEditable {
  nombre: string;
  apellido: string;
  telefono: string;
  direccion: string;
}

@Injectable({ providedIn: 'root' })
export class PerfilService {
  private http = inject(HttpClient);

  private perfil = signal<Perfil | null>(null);
  private perfil$: Observable<Perfil> | null = null;

  get perfilActual() {
    return this.perfil.asReadonly();
  }

  cargarPerfil(): Observable<Perfil> {
    if (!this.perfil$) {
      this.perfil$ = this.http.get<Perfil>(`${environment.apiConfig.bffUri}/bff/perfil`).pipe(
        tap((perfil) => this.perfil.set(perfil)),
        shareReplay(1)
      );
    }
    return this.perfil$;
  }

  actualizarPerfil(datos: PerfilEditable): Observable<Perfil> {
    const actual = this.perfil();
    if (!actual) {
      throw new Error('No hay un perfil cargado para actualizar');
    }

    const body = {
      azureAdObjectId: actual.azureAdObjectId,
      nombre: datos.nombre,
      apellido: datos.apellido,
      email: actual.email,
      telefono: datos.telefono || null,
      direccion: datos.direccion || null,
      rol: actual.rol
    };

    return this.http
      .put<Perfil>(`${environment.apiConfig.gatewayUri}/api/usuarios/${actual.id}`, body)
      .pipe(tap((perfilActualizado) => this.perfil.set(perfilActualizado)));
  }

  limpiar(): void {
    this.perfil.set(null);
    this.perfil$ = null;
  }
}