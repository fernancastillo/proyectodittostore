import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface PerfilUsuario {
  nombre: string;
  apellido: string;
  email: string;
  telefono: string;
  direccion: string;
}

interface StatItem {
  label: string;
  valor: string;
  icon: 'orders' | 'favorites' | 'level';
}

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
})
export class ProfileComponent {
  private readonly perfilGuardado = signal<PerfilUsuario>({
    nombre: 'Benja',
    apellido: 'Entrenador',
    email: 'benja@dittostore.cl',
    telefono: '+56 9 1234 5678',
    direccion: 'Puente Alto, Región Metropolitana',
  });

  readonly editando = signal(false);
  readonly guardadoOk = signal(false);

  /** Copia editable que se muestra en el formulario. */
  draft: PerfilUsuario = { ...this.perfilGuardado() };

  readonly stats: StatItem[] = [
    { label: 'Pedidos realizados', valor: '3', icon: 'orders' },
    { label: 'Cartas favoritas', valor: '12', icon: 'favorites' },
    { label: 'Nivel de entrenador', valor: 'Ditto Oro', icon: 'level' },
  ];

  get perfil(): PerfilUsuario {
    return this.perfilGuardado();
  }

  get iniciales(): string {
    const n = this.perfil.nombre?.charAt(0) ?? '';
    const a = this.perfil.apellido?.charAt(0) ?? '';
    return (n + a).toUpperCase();
  }

  activarEdicion(): void {
    this.draft = { ...this.perfilGuardado() };
    this.editando.set(true);
    this.guardadoOk.set(false);
  }

  cancelarEdicion(): void {
    this.editando.set(false);
  }

  guardarCambios(): void {
    this.perfilGuardado.set({ ...this.draft });
    this.editando.set(false);
    this.guardadoOk.set(true);
    setTimeout(() => this.guardadoOk.set(false), 2500);
  }
}