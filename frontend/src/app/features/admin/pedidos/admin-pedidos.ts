import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-pedidos',
  standalone: true,
  template: `
    <h1 class="page-title">Gestión de pedidos</h1>
    <p class="page-placeholder">Próximamente: ver y actualizar el estado de los envíos.</p>
  `,
  styleUrl: '../shared/admin-page.scss'
})
export class AdminPedidos {}