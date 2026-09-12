import { Routes } from '@angular/router';
import { AdminLayout } from './layout/admin-layout';
import { DashboardAdmin } from './dashboard/dashboard-admin';
import { AdminProductos } from './productos/admin-productos';
import { AdminUsuarios } from './usuarios/admin-usuarios';
import { AdminPedidos } from './pedidos/admin-pedidos';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    component: AdminLayout,
    children: [
      { path: '', component: DashboardAdmin },
      { path: 'productos', component: AdminProductos },
      { path: 'usuarios', component: AdminUsuarios },
      { path: 'pedidos', component: AdminPedidos }
    ]
  }
];