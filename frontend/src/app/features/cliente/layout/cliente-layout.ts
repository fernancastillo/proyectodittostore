import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MsalService, MsalBroadcastService } from '@azure/msal-angular';
import { InteractionStatus } from '@azure/msal-browser';
import { filter } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { CarritoService } from '../../../core/services/carrito.service';

@Component({
  selector: 'app-cliente-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './cliente-layout.html',
  styleUrl: './cliente-layout.scss'
})
export class ClienteLayout implements OnInit {
  private msalService = inject(MsalService);
  private msalBroadcastService = inject(MsalBroadcastService);
  private cdr = inject(ChangeDetectorRef);
  carritoService = inject(CarritoService);

  isLoggedIn = false;
  nombreUsuario = '';
  menuAbierto = false;

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

  toggleMenu(): void {
    this.menuAbierto = !this.menuAbierto;
  }

  cerrarMenu(): void {
    this.menuAbierto = false;
  }

  login(): void {
    this.msalService.loginRedirect({
      scopes: environment.apiConfig.scopes
    });
  }

  logout(): void {
    this.msalService.logoutRedirect();
  }
}