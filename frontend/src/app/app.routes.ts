import { Routes } from '@angular/router';
import { MsalGuard } from '@azure/msal-angular';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home/home.component').then((m) => m.HomeComponent),
    title: 'Ditto Store | Inicio',
  },
  {
    path: 'cajas',
    loadComponent: () => import('./pages/boxes/boxes.component').then((m) => m.BoxesComponent),
    title: 'Ditto Store | Cajas',
  },
  {
    path: 'producto',
    loadComponent: () => import('./pages/catalog/catalog.component').then((m) => m.CatalogComponent),
    title: 'Ditto Store | Productos',
  },
  {
    path: 'carrito',
    loadComponent: () => import('./pages/cart/cart.component').then((m) => m.CartComponent),
    title: 'Ditto Store | Carrito',
  },
  {
    path: 'pago',
    loadComponent: () =>
      import('./pages/checkout/checkout.component').then((m) => m.CheckoutComponent),
    title: 'Ditto Store | Pago',
  },
  {
    path: 'cuenta',
    loadComponent: () =>
      import('./pages/profile/profile.component').then((m) => m.ProfileComponent),
    title: 'Ditto Store | Cuenta',
  },
  {
    path: 'contacto',
    loadComponent: () =>
      import('./pages/contact/contact.component').then((m) => m.ContactComponent),
    title: 'Ditto Store | Contacto',
  },
  {
    path: 'admin',
    canActivate: [MsalGuard, roleGuard('ADMIN')],
    loadChildren: () => import('./features/admin/admin.routes').then((m) => m.ADMIN_ROUTES),
  },
  { path: '**', redirectTo: '' },
];