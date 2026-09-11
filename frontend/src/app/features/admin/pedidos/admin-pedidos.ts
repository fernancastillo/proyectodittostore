import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PedidoAdminService, Pedido, EstadoPedido } from '../../../core/services/pedido-admin.service';

@Component({
  selector: 'app-admin-pedidos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-pedidos.html',
  styleUrl: './admin-pedidos.scss'
})
export class AdminPedidos implements OnInit {
  private pedidoService = inject(PedidoAdminService);
  private cdr = inject(ChangeDetectorRef);

  pedidos: Pedido[] = [];
  cargando = true;
  error: string | null = null;

  filtroTexto = '';
  filtroEstado = '';
  ordenarPor = '';

  mostrandoModal = false;
  guardando = false;
  erroresValidacion: string[] = [];
  pedidoActual: Pedido | null = null;
  estadoSeleccionado: EstadoPedido | null = null;

  readonly estados: EstadoPedido[] = ['PENDIENTE', 'CONFIRMADO', 'ENVIADO', 'ENTREGADO', 'CANCELADO'];

  ngOnInit(): void {
    this.cargarPedidos();
  }

  cargarPedidos(): void {
    this.cargando = true;
    this.pedidoService.obtenerTodos().subscribe({
      next: (data) => {
        this.pedidos = data;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = `No se pudieron cargar los pedidos (${err.status})`;
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  get pedidosFiltrados(): Pedido[] {
    let resultado = [...this.pedidos];

    if (this.filtroTexto.trim()) {
      const texto = this.filtroTexto.trim().toLowerCase();
      resultado = resultado.filter(p =>
        p.id.toString().includes(texto) ||
        p.usuarioId.toString().includes(texto) ||
        p.direccionEnvio?.toLowerCase().includes(texto)
      );
    }

    if (this.filtroEstado) {
      resultado = resultado.filter(p => p.estado === this.filtroEstado);
    }

    if (this.ordenarPor) {
      resultado.sort((a, b) => {
        switch (this.ordenarPor) {
          case 'fecha-asc':
            return new Date(a.fechaPedido).getTime() - new Date(b.fechaPedido).getTime();
          case 'fecha-desc':
            return new Date(b.fechaPedido).getTime() - new Date(a.fechaPedido).getTime();
          case 'total-asc':
            return a.total - b.total;
          case 'total-desc':
            return b.total - a.total;
          default:
            return 0;
        }
      });
    }

    return resultado;
  }

  limpiarFiltros(): void {
    this.filtroTexto = '';
    this.filtroEstado = '';
    this.ordenarPor = '';
  }

  formatearEstado(estado: string): string {
    if (!estado) return '';
    return estado.charAt(0).toUpperCase() + estado.slice(1).toLowerCase();
  }

  claseEstado(estado: string): string {
    const mapa: Record<string, string> = {
      PENDIENTE: 'estado-pill--pendiente',
      CONFIRMADO: 'estado-pill--confirmado',
      ENVIADO: 'estado-pill--enviado',
      ENTREGADO: 'estado-pill--entregado',
      CANCELADO: 'estado-pill--cancelado'
    };
    return `estado-pill ${mapa[estado] ?? ''}`;
  }

  formatearFecha(fecha: string): string {
    if (!fecha) return '';
    return new Date(fecha).toLocaleString('es-CL', {
      day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
    });
  }

  formatearMonto(monto: number): string {
    return (monto ?? 0).toLocaleString('es-CL');
  }

  abrirModalEstado(pedido: Pedido): void {
    this.erroresValidacion = [];
    this.pedidoActual = pedido;
    this.estadoSeleccionado = pedido.estado;
    this.mostrandoModal = true;
  }

  cerrarModal(): void {
    this.mostrandoModal = false;
    this.pedidoActual = null;
    this.estadoSeleccionado = null;
    this.erroresValidacion = [];
  }

  guardar(): void {
    this.erroresValidacion = [];

    if (!this.pedidoActual?.id || !this.estadoSeleccionado) {
      this.erroresValidacion.push('Selecciona un estado.');
      return;
    }

    this.guardando = true;
    this.pedidoService.actualizarEstado(this.pedidoActual.id, this.estadoSeleccionado).subscribe({
      next: () => {
        this.guardando = false;
        this.mostrandoModal = false;
        this.pedidoActual = null;
        this.estadoSeleccionado = null;
        this.cargarPedidos();
      },
      error: (err) => {
        this.guardando = false;
        this.error = `No se pudo actualizar el estado del pedido (${err.status})`;
        this.cdr.detectChanges();
      }
    });
  }
}