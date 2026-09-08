import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { MsalService, MsalBroadcastService } from '@azure/msal-angular';
import { InteractionStatus } from '@azure/msal-browser';
import { filter } from 'rxjs/operators';
import { PerfilService } from './core/services/perfil.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  private msalService = inject(MsalService);
  private msalBroadcastService = inject(MsalBroadcastService);
  private router = inject(Router);
  private perfilService = inject(PerfilService);

  ngOnInit(): void {
    this.msalService.handleRedirectObservable().subscribe({
      next: (result) => {
        if (result?.account) {
          this.msalService.instance.setActiveAccount(result.account);
        }
        this.redirigirSiEsAdmin();
      },
      error: (error) => console.error('Error procesando el redirect:', error)
    });

    this.msalBroadcastService.inProgress$
      .pipe(filter((status) => status === InteractionStatus.None))
      .subscribe(() => {
        this.redirigirSiEsAdmin();
      });
  }

  private redirigirSiEsAdmin(): void {
    const cuentas = this.msalService.instance.getAllAccounts();
    if (cuentas.length === 0) {
      return;
    }

    this.perfilService.cargarPerfil().subscribe({
      next: (perfil) => {
        if (perfil.rol === 'ADMIN') {
          this.router.navigateByUrl('/admin');
        }
      },
      error: (err) => console.error('Error cargando perfil:', err)
    });
  }
}