import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService, DashboardMetricas } from '../../../core/services/dashboard.service';

@Component({
  selector: 'app-dashboard-admin',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-admin.html',
  styleUrl: './dashboard-admin.scss'
})
export class DashboardAdmin implements OnInit {
  private dashboardService = inject(DashboardService);
  private cdr = inject(ChangeDetectorRef);

  metricas: DashboardMetricas | null = null;
  cargando = true;
  error: string | null = null;

  ngOnInit(): void {
    this.dashboardService.obtenerMetricas().subscribe({
      next: (data) => {
        this.metricas = data;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = `No se pudieron cargar las metricas (${err.status})`;
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  claseParaEstado(estado: string): string {
    const mapa: Record<string, string> = {
      PENDIENTE: 'badge badge--pendiente',
      CONFIRMADO: 'badge badge--confirmado',
      ENVIADO: 'badge badge--enviado',
      ENTREGADO: 'badge badge--entregado',
      CANCELADO: 'badge badge--cancelado'
    };
    return mapa[estado] ?? 'badge';
  }
}