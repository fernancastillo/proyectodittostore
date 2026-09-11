import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UsuarioAdminService, Usuario, RolUsuario } from '../../../core/services/usuario-admin.service';

@Component({
  selector: 'app-admin-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-usuarios.html',
  styleUrl: './admin-usuarios.scss'
})
export class AdminUsuarios implements OnInit {
  private usuarioService = inject(UsuarioAdminService);
  private cdr = inject(ChangeDetectorRef);

  usuarios: Usuario[] = [];
  cargando = true;
  error: string | null = null;

  filtroTexto = '';
  filtroRol = '';
  ordenarPor = '';

  mostrandoModal = false;
  guardando = false;
  erroresValidacion: string[] = [];
  usuarioActual: Usuario | null = null;

  readonly roles: RolUsuario[] = ['CLIENTE', 'ADMIN'];

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    this.cargando = true;
    this.usuarioService.obtenerTodos().subscribe({
      next: (data) => {
        this.usuarios = data;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = `No se pudieron cargar los usuarios (${err.status})`;
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  get usuariosFiltrados(): Usuario[] {
    let resultado = [...this.usuarios];

    if (this.filtroTexto.trim()) {
      const texto = this.filtroTexto.trim().toLowerCase();
      resultado = resultado.filter(u =>
        u.nombre?.toLowerCase().includes(texto) ||
        u.apellido?.toLowerCase().includes(texto) ||
        u.email?.toLowerCase().includes(texto)
      );
    }

    if (this.filtroRol) {
      resultado = resultado.filter(u => u.rol === this.filtroRol);
    }

    if (this.ordenarPor) {
      resultado.sort((a, b) => {
        switch (this.ordenarPor) {
          case 'nombre-asc':
            return a.nombre.localeCompare(b.nombre);
          case 'nombre-desc':
            return b.nombre.localeCompare(a.nombre);
          case 'email-asc':
            return a.email.localeCompare(b.email);
          case 'email-desc':
            return b.email.localeCompare(a.email);
          default:
            return 0;
        }
      });
    }

    return resultado;
  }

  limpiarFiltros(): void {
    this.filtroTexto = '';
    this.filtroRol = '';
    this.ordenarPor = '';
  }

  formatearRol(rol: string): string {
    if (!rol) return '';
    return rol.charAt(0).toUpperCase() + rol.slice(1).toLowerCase();
  }

  validarUsuario(): boolean {
    this.erroresValidacion = [];

    if (!this.usuarioActual) {
      return false;
    }

    if (!this.usuarioActual.nombre || this.usuarioActual.nombre.trim().length < 2) {
      this.erroresValidacion.push('El nombre debe tener al menos 2 caracteres.');
    }

    if (!this.usuarioActual.apellido || this.usuarioActual.apellido.trim().length < 2) {
      this.erroresValidacion.push('El apellido debe tener al menos 2 caracteres.');
    }

    if (!this.usuarioActual.rol) {
      this.erroresValidacion.push('Selecciona un rol.');
    }

    return this.erroresValidacion.length === 0;
  }

  abrirModalEditar(usuario: Usuario): void {
    this.erroresValidacion = [];
    this.usuarioActual = { ...usuario };
    this.mostrandoModal = true;
  }

  cerrarModal(): void {
    this.mostrandoModal = false;
    this.usuarioActual = null;
    this.erroresValidacion = [];
  }

  guardar(): void {
    if (!this.usuarioActual?.id || !this.validarUsuario()) {
      return;
    }

    this.guardando = true;

    // El correo, el id y el azureAdObjectId nunca se editan desde este formulario.
    this.usuarioService.actualizar(this.usuarioActual.id, this.usuarioActual).subscribe({
      next: () => {
        this.guardando = false;
        this.mostrandoModal = false;
        this.usuarioActual = null;
        this.cargarUsuarios();
      },
      error: (err) => {
        this.guardando = false;
        this.error = `No se pudo guardar el usuario (${err.status})`;
        this.cdr.detectChanges();
      }
    });
  }

  eliminar(usuario: Usuario): void {
    if (!usuario.id) return;
    const confirmar = confirm(`¿Eliminar a "${usuario.nombre} ${usuario.apellido}"? Esta acción no se puede deshacer.`);
    if (!confirmar) return;

    this.usuarioService.eliminar(usuario.id).subscribe({
      next: () => this.cargarUsuarios(),
      error: (err) => {
        this.error = `No se pudo eliminar el usuario (${err.status})`;
        this.cdr.detectChanges();
      }
    });
  }
}