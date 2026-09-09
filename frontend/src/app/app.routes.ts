import { Routes } from '@angular/router';

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
    loadComponent: () =>
      import('./pages/coming-soon/coming-soon.component').then((m) => m.ComingSoonComponent),
    data: { title: 'Tu carrito' },
    title: 'Ditto Store | Carrito',
  },
  {
    path: 'cuenta',
    loadComponent: () =>
      import('./pages/coming-soon/coming-soon.component').then((m) => m.ComingSoonComponent),
    data: { title: 'Tu cuenta' },
    title: 'Ditto Store | Cuenta',
  },
  {
    path: 'contacto',
    loadComponent: () =>
      import('./pages/contact/contact.component').then((m) => m.ContactComponent),
    title: 'Ditto Store | Contacto',
  },
  { path: '**', redirectTo: '' },
];
