import { Component, inject } from '@angular/core';
import { MsalService } from '@azure/msal-angular';

@Component({
  selector: 'app-login',
  standalone: true,
  template: `
    <button (click)="login()">Iniciar sesión</button>
    <button (click)="logout()">Cerrar sesión</button>
  `
})
export class Login {
  private msalService = inject(MsalService);

  login(): void {
    this.msalService.loginRedirect();
  }

  logout(): void {
    this.msalService.logoutRedirect();
  }
}