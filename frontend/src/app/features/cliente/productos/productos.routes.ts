import { Routes } from '@angular/router';
import { CatalogoProductos } from './catalogo/catalogo-productos';
import { DetalleProducto } from './detalle/detalle-producto';

export const PRODUCTOS_ROUTES: Routes = [
  { path: '', component: CatalogoProductos },
  { path: ':id', component: DetalleProducto }
];