import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { MsalService, MsalBroadcastService } from '@azure/msal-angular';
import { InteractionStatus } from '@azure/msal-browser';
import { filter } from 'rxjs/operators';
import { JsonPipe } from '@angular/common';
import { environment } from '../../environments/environment';
import { PerfilService } from '../core/services/perfil.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [JsonPipe],
  templateUrl: './home.html'
})
export class Home implements OnInit {
  private msalService = inject(MsalService);
  private msalBroadcastService = inject(MsalBroadcastService);
  private cdr = inject(ChangeDetectorRef);
  private http = inject(HttpClient);
  private perfilService = inject(PerfilService);

  isLoggedIn = false;
  nombreUsuario = '';
  perfilResultado: any = null;
  perfilError: string | null = null;

  ngOnInit(): void {
    this.actualizarEstadoLogin();

    this.msalBroadcastService.inProgress$
      .pipe(filter((status) => status === InteractionStatus.None))
      .subscribe(() => {
        this.actualizarEstadoLogin();
      });
  }

  private actualizarEstadoLogin(): void {
    const cuentas = this.msalService.instance.getAllAccounts();
    this.isLoggedIn = cuentas.length > 0;
    this.nombreUsuario = cuentas.length > 0 ? cuentas[0].name ?? cuentas[0].username : '';
    this.cdr.detectChanges();
  }

  login(): void {
    this.msalService.loginRedirect({
      scopes: environment.apiConfig.scopes
    });
  }

  logout(): void {
    this.perfilService.limpiar();
    this.msalService.logoutRedirect();
  }

  probarPerfil(): void {
    this.perfilError = null;
    this.perfilResultado = null;

    this.http.get(`${environment.apiConfig.bffUri}/bff/perfil`).subscribe({
      next: (data) => {
        this.perfilResultado = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.perfilError = `Error ${err.status}: ${err.message}`;
        this.cdr.detectChanges();
      }
    });
  }
}