import { Routes } from '@angular/router';
import { MsalGuard } from '@azure/msal-angular';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./home/home').then(m => m.Home)
  },
  {
    path: 'cliente/carrito',
    loadChildren: () => import('./features/cliente/carrito/carrito.routes').then((m) => m.CARRITO_ROUTES),
  },
  {
    path: 'admin',
    canActivate: [MsalGuard, roleGuard('ADMIN')],
    loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES)
  }
];