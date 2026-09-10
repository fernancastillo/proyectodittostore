import { Component, OnInit, inject } from '@angular/core';
import { PerfilService } from '../../../core/services/perfil.service';

@Component({
  selector: 'app-perfil',
  standalone: true,
  templateUrl: './perfil.html'
})
export class Perfil implements OnInit {
  private perfilService = inject(PerfilService);

  perfil = this.perfilService.perfilActual;

  ngOnInit(): void {
    this.perfilService.cargarPerfil().subscribe({
      error: (err) => console.error('Error cargando perfil:', err)
    });
  }
}