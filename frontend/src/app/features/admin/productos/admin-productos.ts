import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-productos',
  standalone: true,
  template: `
    <h1 class="page-title">Gestión de productos</h1>
    <p class="page-placeholder">Próximamente: listar, crear y editar productos.</p>
  `,
  styleUrl: '../shared/admin-page.scss'
})
export class AdminProductos {}