import { Component } from '@angular/core';
import { Routes } from '@angular/router';

@Component({
  selector: 'app-inicio-cliente',
  standalone: true,
  template: `<div style="padding: 2rem;"><h2>Inicio Cliente</h2><p>Bienvenido, esta es la seccion de cliente.</p></div>`
})
class InicioClientePlaceholder {}

export const CLIENTE_ROUTES: Routes = [
  { path: '', component: InicioClientePlaceholder }
];