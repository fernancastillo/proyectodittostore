import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProductoService, Producto, TipoCaja } from '../../../../core/services/producto.service';

@Component({
  selector: 'app-catalogo-productos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './catalogo-productos.html',
  styleUrl: './catalogo-productos.scss'
})
export class CatalogoProductos implements OnInit {
  private productoService = inject(ProductoService);
  private cdr = inject(ChangeDetectorRef);

  productos: Producto[] = [];
  cargando = true;
  error: string | null = null;

  filtroNombre = '';
  filtroTipo = '';
  ordenarPor = '';

  readonly tiposCaja: TipoCaja[] = ['BOOSTER_BOX', 'ELITE_TRAINER_BOX', 'DISPLAY', 'FAT_PACK'];

  ngOnInit(): void {
    this.cargarProductos();
  }

  cargarProductos(): void {
    this.cargando = true;
    this.error = null;

    this.productoService.listar().subscribe({
      next: (data) => {
        this.productos = data;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = `No se pudieron cargar los productos (${err.status})`;
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  get productosFiltrados(): Producto[] {
    let resultado = [...this.productos];

    if (this.filtroNombre.trim()) {
      const nom = this.filtroNombre.trim().toLowerCase();
      resultado = resultado.filter(p => p.nombre?.toLowerCase().includes(nom));
    }

    if (this.filtroTipo) {
      resultado = resultado.filter(p => p.tipoCaja === this.filtroTipo);
    }

    if (this.ordenarPor) {
      resultado.sort((a, b) => {
        switch (this.ordenarPor) {
          case 'nombre-asc':
            return a.nombre.localeCompare(b.nombre);
          case 'nombre-desc':
            return b.nombre.localeCompare(a.nombre);
          case 'precio-asc':
            return a.precio - b.precio;
          case 'precio-desc':
            return b.precio - a.precio;
          default:
            return 0;
        }
      });
    }

    return resultado;
  }

  limpiarFiltros(): void {
    this.filtroNombre = '';
    this.filtroTipo = '';
    this.ordenarPor = '';
  }

  formatearTipoCaja(tipo: string): string {
    if (!tipo) return '';
    return tipo
      .toLowerCase()
      .split('_')
      .map(palabra => palabra.charAt(0).toUpperCase() + palabra.slice(1))
      .join(' ');
  }

  formatearPrecio(precio: number): string {
    return (precio ?? 0).toLocaleString('es-CL');
  }
}