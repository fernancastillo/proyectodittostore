import { Routes } from '@angular/router'; 
import { MsalGuard } from '@azure/msal-angular'; 
import { roleGuard } from './core/guards/role.guard';
import { ClienteLayout } from './features/cliente/layout/cliente-layout';

export const routes: Routes = [
  {
    path: '',
    component: ClienteLayout,
    children: [
      { path: '', loadComponent: () => import('./home/home').then(m => m.Home) },
      { path: 'productos', loadChildren: () => import('./features/cliente/productos/productos.routes').then(m => m.PRODUCTOS_ROUTES) },
      { path: 'contacto', loadComponent: () => import('./features/cliente/contacto/contacto').then(m => m.Contacto) },
      {
        path: 'carrito',
        loadComponent: () => import('./features/cliente/carrito/carrito.component').then(m => m.CarritoComponent)
      },
      {
        path: 'perfil',
        canActivate: [MsalGuard],
        loadComponent: () => import('./features/cliente/perfil/perfil').then(m => m.Perfil)
      }
    ]
  },
  {
    path: 'admin',
    canActivate: [MsalGuard, roleGuard('ADMIN')],
    loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES)
  }
];