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

  limpiar(): void {
    this.perfil.set(null);
    this.perfil$ = null;
  }
}