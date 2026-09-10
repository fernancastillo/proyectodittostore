import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductoAdminService, Producto, TipoCaja, Idioma } from '../../../core/services/producto-admin.service';

@Component({
  selector: 'app-admin-productos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-productos.html',
  styleUrl: './admin-productos.scss'
})
export class AdminProductos implements OnInit {
  private productoService = inject(ProductoAdminService);
  private cdr = inject(ChangeDetectorRef);

  productos: Producto[] = [];
  cargando = true;
  error: string | null = null;

  filtroNombre = '';
  filtroTipo = '';
  filtroPrecioMin: number | null = null;
  filtroPrecioMax: number | null = null;
  ordenarPor = '';

  mostrandoModal = false;
  modoEdicion = false;
  guardando = false;
  erroresValidacion: string[] = [];
  productoActual: Producto = this.productoVacio();

  readonly tiposCaja: TipoCaja[] = ['BOOSTER_BOX', 'ELITE_TRAINER_BOX', 'DISPLAY', 'FAT_PACK'];
  readonly idiomas: Idioma[] = ['ESPANOL', 'INGLES', 'JAPONES'];

  ngOnInit(): void {
    this.cargarProductos();
  }

  cargarProductos(): void {
    this.cargando = true;
    this.productoService.obtenerTodos().subscribe({
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

    if (this.filtroPrecioMin !== null && this.filtroPrecioMin !== undefined) {
      resultado = resultado.filter(p => p.precio >= (this.filtroPrecioMin ?? 0));
    }
    if (this.filtroPrecioMax !== null && this.filtroPrecioMax !== undefined) {
      resultado = resultado.filter(p => p.precio <= (this.filtroPrecioMax ?? Infinity));
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
          case 'stock-asc':
            return a.stock - b.stock;
          case 'stock-desc':
            return b.stock - a.stock;
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
    this.filtroPrecioMin = null;
    this.filtroPrecioMax = null;
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

  validarProducto(): boolean {
    this.erroresValidacion = [];

    if (!this.productoActual.nombre || this.productoActual.nombre.trim().length < 3) {
      this.erroresValidacion.push('El nombre del producto debe tener al menos 3 caracteres.');
    }

    if (!this.productoActual.descripcion || this.productoActual.descripcion.trim().length < 10) {
      this.erroresValidacion.push('La descripción debe tener al menos 10 caracteres.');
    }

    if (!this.productoActual.coleccionSet || this.productoActual.coleccionSet.trim().length < 3) {
      this.erroresValidacion.push('La colección / set debe tener al menos 3 caracteres.');
    }

    if (this.productoActual.cantidadSobres !== undefined && this.productoActual.cantidadSobres !== null && this.productoActual.cantidadSobres <= 0) {
      this.erroresValidacion.push('La cantidad de sobres debe ser mayor a 0.');
    }

    if (this.productoActual.cartasPorSobre !== undefined && this.productoActual.cartasPorSobre !== null && this.productoActual.cartasPorSobre <= 0) {
      this.erroresValidacion.push('La cantidad de cartas por sobre debe ser mayor a 0.');
    }

    if (this.productoActual.precio === undefined || this.productoActual.precio === null || this.productoActual.precio < 0) {
      this.erroresValidacion.push('El precio no puede ser menor a 0.');
    }

    if (this.productoActual.stock === undefined || this.productoActual.stock === null || this.productoActual.stock < 0) {
      this.erroresValidacion.push('El stock no puede ser menor a 0.');
    }

    return this.erroresValidacion.length === 0;
  }

  abrirModalCrear(): void {
    this.modoEdicion = false;
    this.erroresValidacion = [];
    this.productoActual = this.productoVacio();
    this.mostrandoModal = true;
  }

  abrirModalEditar(producto: Producto): void {
    this.modoEdicion = true;
    this.erroresValidacion = [];
    this.productoActual = { ...producto };
    this.mostrandoModal = true;
  }

  cerrarModal(): void {
    this.mostrandoModal = false;
    this.erroresValidacion = [];
  }

  guardar(): void {
    if (!this.validarProducto()) {
      return;
    }

    this.guardando = true;
    const operacion = this.modoEdicion && this.productoActual.id
      ? this.productoService.actualizar(this.productoActual.id, this.productoActual)
      : this.productoService.crear(this.productoActual);

    operacion.subscribe({
      next: () => {
        this.guardando = false;
        this.mostrandoModal = false;
        this.cargarProductos();
      },
      error: (err) => {
        this.guardando = false;
        this.error = `No se pudo guardar el producto (${err.status})`;
        this.cdr.detectChanges();
      }
    });
  }

  eliminar(producto: Producto): void {
    if (!producto.id) return;
    const confirmar = confirm(`¿Eliminar "${producto.nombre}"? Esta acción no se puede deshacer.`);
    if (!confirmar) return;

    this.productoService.eliminar(producto.id).subscribe({
      next: () => this.cargarProductos(),
      error: (err) => {
        this.error = `No se pudo eliminar el producto (${err.status})`;
        this.cdr.detectChanges();
      }
    });
  }

  private productoVacio(): Producto {
    return {
      nombre: '',
      descripcion: '',
      coleccionSet: '',
      tipoCaja: 'BOOSTER_BOX',
      cantidadSobres: undefined,
      cartasPorSobre: undefined,
      idioma: 'ESPANOL',
      precio: 0,
      stock: 0,
      imagenUrl: ''
    };
  }
}