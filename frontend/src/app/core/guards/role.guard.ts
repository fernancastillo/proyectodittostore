import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, catchError, of } from 'rxjs';
import { PerfilService } from '../services/perfil.service';

export function roleGuard(rolRequerido: 'CLIENTE' | 'ADMIN'): CanActivateFn {
  return () => {
    const perfilService = inject(PerfilService);
    const router = inject(Router);

    return perfilService.cargarPerfil().pipe(
      map((perfil) => {
        if (perfil.rol === rolRequerido) {
          return true;
        }
        return router.createUrlTree(['/']);
      }),
      catchError(() => of(router.createUrlTree(['/'])))
    );
  };
}