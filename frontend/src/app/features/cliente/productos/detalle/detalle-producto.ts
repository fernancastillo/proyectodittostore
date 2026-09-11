import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ProductoService, Producto } from '../../../../core/services/producto.service';

@Component({
  selector: 'app-detalle-producto',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './detalle-producto.html',
  styleUrl: './detalle-producto.scss'
})
export class DetalleProducto implements OnInit {
  private route = inject(ActivatedRoute);
  private productoService = inject(ProductoService);
  private cdr = inject(ChangeDetectorRef);

  producto: Producto | null = null;
  cargando = true;
  error: string | null = null;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!id) {
      this.error = 'Producto no encontrado.';
      this.cargando = false;
      return;
    }

    this.productoService.obtenerPorId(id).subscribe({
      next: (data) => {
        this.producto = data;
        this.cargando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = err.status === 404
          ? 'Este producto no existe o fue eliminado.'
          : `No se pudo cargar el producto (${err.status})`;
        this.cargando = false;
        this.cdr.detectChanges();
      }
    });
  }

  formatearTipoCaja(tipo: string): string {
    if (!tipo) return '';
    return tipo
      .toLowerCase()
      .split('_')
      .map(palabra => palabra.charAt(0).toUpperCase() + palabra.slice(1))
      .join(' ');
  }

  formatearIdioma(idioma: string): string {
    if (!idioma) return '';

    const mapaIdiomas: Record<string, string> = {
      'ESPANOL': 'Español',
      'INGLES': 'Inglés',
      'JAPONES': 'Japonés'
    };

    return mapaIdiomas[idioma] ?? (idioma.charAt(0).toUpperCase() + idioma.slice(1).toLowerCase());
  }

  formatearPrecio(precio: number): string {
    return (precio ?? 0).toLocaleString('es-CL');
  }
}