import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type RolUsuario = 'CLIENTE' | 'ADMIN';

export interface Usuario {
  id?: number;
  azureAdObjectId?: string;
  nombre: string;
  apellido: string;
  email: string;
  telefono?: string;
  direccion?: string;
  rol: RolUsuario;
}

@Injectable({ providedIn: 'root' })
export class UsuarioAdminService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiConfig.bffUri}/bff/admin/usuarios`;

  obtenerTodos(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.baseUrl);
  }

  actualizar(id: number, usuario: Usuario): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.baseUrl}/${id}`, usuario);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}