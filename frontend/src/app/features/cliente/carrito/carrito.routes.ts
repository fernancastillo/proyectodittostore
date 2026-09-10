import { Routes } from '@angular/router';

export const CARRITO_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./carrito.component').then((m) => m.CarritoComponent),
    title: 'Ditto Store | Carrito',
  },
];