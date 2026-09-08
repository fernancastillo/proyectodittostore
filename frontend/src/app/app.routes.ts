import { Routes } from '@angular/router';
import { MsalGuard } from '@azure/msal-angular';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./home/home').then(m => m.Home)
  },
  {
    path: 'admin',
    canActivate: [MsalGuard, roleGuard('ADMIN')],
    loadChildren: () => import('./features/admin/dashboard/dashboard.routes').then(m => m.ADMIN_ROUTES)
  }
];