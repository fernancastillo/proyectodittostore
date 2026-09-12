import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MsalService } from '@azure/msal-angular';
import { ProductoService, Producto } from '../../../../core/services/producto.service';
import { ReviewsService, Review } from '../../../../core/services/reviews.service';
import { CarritoService } from '../../../../core/services/carrito.service';

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
  private reviewsService = inject(ReviewsService);
  private carritoService = inject(CarritoService);
  private msalService = inject(MsalService);
  private cdr = inject(ChangeDetectorRef);

  producto: Producto | null = null;
  cargando = true;
  error: string | null = null;

  reviews: Review[] = [];
  cargandoReviews = true;

  isLoggedIn = false;
  agregandoAlCarrito = false;
  mensajeCarrito: string | null = null;
  errorCarrito: string | null = null;

  ngOnInit(): void {
    this.isLoggedIn = this.msalService.instance.getAllAccounts().length > 0;

    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!id) {
      this.error = 'Producto no encontrado.';
      this.cargando = false;
      this.cargandoReviews = false;
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

    this.cargarReviews(id);
  }

  private cargarReviews(productoId: number): void {
    this.cargandoReviews = true;
    this.reviewsService.obtenerPorProducto(productoId).subscribe({
      next: (data) => {
        this.reviews = data;
        this.cargandoReviews = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.reviews = [];
        this.cargandoReviews = false;
        this.cdr.detectChanges();
      }
    });
  }

  get puedeAgregarAlCarrito(): boolean {
    return !!this.producto && this.producto.stock > 0 && this.isLoggedIn;
  }

  agregarAlCarrito(): void {
    if (!this.producto || !this.puedeAgregarAlCarrito) {
      return;
    }

    this.agregandoAlCarrito = true;
    this.mensajeCarrito = null;
    this.errorCarrito = null;

    this.carritoService.agregarItem(this.producto.id, 1).subscribe({
      next: () => {
        this.agregandoAlCarrito = false;
        this.mensajeCarrito = 'Producto agregado al carrito.';
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.agregandoAlCarrito = false;
        this.errorCarrito = err.error?.mensaje ?? `No se pudo agregar el producto (${err.status})`;
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