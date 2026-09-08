import { Component, inject } from '@angular/core';
import { Routes } from '@angular/router';
import { MsalService } from '@azure/msal-angular';
import { PerfilService } from '../../../core/services/perfil.service';

@Component({
  selector: 'app-dashboard-admin',
  standalone: true,
  template: `
    <div style="padding: 2rem;">
      <h2>Dashboard Admin</h2>
      <p>Bienvenido, esta es la seccion de administrador.</p>
      <button (click)="logout()">Cerrar sesión</button>
    </div>
  `
})
class DashboardAdminPlaceholder {
  private msalService = inject(MsalService);
  private perfilService = inject(PerfilService);

  logout(): void {
    this.perfilService.limpiar();
    this.msalService.logoutRedirect();
  }
}

export const ADMIN_ROUTES: Routes = [
  { path: '', component: DashboardAdminPlaceholder }
];