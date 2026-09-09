import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface NavLink {
  label: string;
  path: string;
  exact?: boolean;
}

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss',
})
export class NavbarComponent {
  readonly links: NavLink[] = [
    { label: 'Inicio', path: '/', exact: true },
    { label: 'Cajas', path: '/cajas' },
    { label: 'Producto', path: '/producto' },
    { label: 'Carrito', path: '/carrito' },
    { label: 'Contacto', path: '/contacto' },
  ];

  readonly menuOpen = signal(false);

  toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }
}