import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router'; // 1. Importar RouterLink
import { forkJoin } from 'rxjs';
import { PerfilService } from '../../../core/services/perfil.service';
import { PedidoService, Pedido } from '../../../core/services/pedido.service';
import { ProductoService, Producto } from '../../../core/services/producto.service';
import { ReviewsService, Review, ReviewRequest } from '../../../core/services/reviews.service';

interface ItemConProducto {
  id: number;
  productoId: number;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  producto: Producto | null;
}

interface PedidoConProductos extends Omit<Pedido, 'items'> {
  items: ItemConProducto[];
}

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink], // 2. Agregar RouterLink aquí
  templateUrl: './perfil.html',
  styleUrl: './perfil.scss'
})
export class Perfil implements OnInit {
  private perfilService = inject(PerfilService);
  private pedidoService = inject(PedidoService);
  private productoService = inject(ProductoService);
  private reviewsService = inject(ReviewsService);

  perfil = this.perfilService.perfilActual;

  editando = signal(false);
  guardandoPerfil = signal(false);
  errorPerfil = signal<string | null>(null);

  datosEditables = { nombre: '', apellido: '', telefono: '', direccion: '' };

  pedidos = signal<PedidoConProductos[]>([]);
  cargandoPedidos = signal(false);
  errorPedidos = signal<string | null>(null);

  reviewsPorProducto = signal<Map<number, Review>>(new Map());
  formsReview: Record<number, { calificacion: number; comentario: string }> = {};
  productosEditandoReview = signal<Set<number>>(new Set());
  guardandoReview = signal<number | null>(null);
  errorReview = signal<string | null>(null);

  ngOnInit(): void {
    this.perfilService.cargarPerfil().subscribe({
      next: (perfil) => this.cargarPedidosYReviews(perfil.id),
      error: (err) => console.error('Error cargando perfil:', err)
    });
  }

  iniciarEdicion(): void {
    const p = this.perfil();
    if (!p) return;
    this.datosEditables = {
      nombre: p.nombre,
      apellido: p.apellido,
      telefono: p.telefono ?? '',
      direccion: p.direccion ?? ''
    };
    this.errorPerfil.set(null);
    this.editando.set(true);
  }

  cancelarEdicion(): void {
    this.editando.set(false);
    this.errorPerfil.set(null);
  }

  guardarPerfil(form: NgForm): void {
    if (form.invalid) {
      Object.values(form.controls).forEach((c) => c.markAsTouched());
      return;
    }

    this.guardandoPerfil.set(true);
    this.errorPerfil.set(null);

    this.perfilService
      .actualizarPerfil({
        nombre: this.datosEditables.nombre.trim(),
        apellido: this.datosEditables.apellido.trim(),
        telefono: this.datosEditables.telefono.trim(),
        direccion: this.datosEditables.direccion.trim()
      })
      .subscribe({
        next: () => {
          this.guardandoPerfil.set(false);
          this.editando.set(false);
        },
        error: (err) => {
          this.guardandoPerfil.set(false);
          this.errorPerfil.set(`No se pudo actualizar el perfil (error ${err.status})`);
        }
      });
  }

  private cargarPedidosYReviews(usuarioId: number): void {
    this.cargandoPedidos.set(true);
    this.errorPedidos.set(null);

    forkJoin({
      pedidos: this.pedidoService.obtenerPorUsuario(usuarioId),
      reviews: this.reviewsService.obtenerPorUsuario(usuarioId)
    }).subscribe({
      next: ({ pedidos, reviews }) => {
        const mapaReviews = new Map<number, Review>();
        reviews.forEach((r) => mapaReviews.set(r.productoId, r));
        this.reviewsPorProducto.set(mapaReviews);
        this.enriquecerPedidos(pedidos);
      },
      error: (err) => {
        this.cargandoPedidos.set(false);
        this.errorPedidos.set(`No se pudieron cargar tus pedidos (error ${err.status})`);
      }
    });
  }

  private enriquecerPedidos(pedidos: Pedido[]): void {
    if (pedidos.length === 0) {
      this.pedidos.set([]);
      this.cargandoPedidos.set(false);
      return;
    }

    const idsUnicos = Array.from(new Set(pedidos.flatMap((p) => p.items.map((i) => i.productoId))));

    forkJoin(idsUnicos.map((id) => this.productoService.obtenerPorId(id))).subscribe({
      next: (productos) => {
        const mapaProductos = new Map<number, Producto>();
        productos.forEach((prod) => mapaProductos.set(prod.id, prod));

        const pedidosConProductos: PedidoConProductos[] = pedidos
          .map((p) => ({
            ...p,
            items: p.items.map((item) => ({
              ...item,
              producto: mapaProductos.get(item.productoId) ?? null
            }))
          }))
          .sort((a, b) => new Date(b.fechaPedido).getTime() - new Date(a.fechaPedido).getTime());

        this.pedidos.set(pedidosConProductos);
        this.cargandoPedidos.set(false);
      },
      error: (err) => {
        this.cargandoPedidos.set(false);
        this.errorPedidos.set(`No se pudieron cargar los productos del pedido (error ${err.status})`);
      }
    });
  }

  reviewDe(productoId: number): Review | undefined {
    return this.reviewsPorProducto().get(productoId);
  }

  editandoReview(productoId: number): boolean {
    return this.productosEditandoReview().has(productoId);
  }

  abrirFormularioReview(productoId: number): void {
    const existente = this.reviewDe(productoId);
    this.formsReview[productoId] = {
      calificacion: existente?.calificacion ?? 5,
      comentario: existente?.comentario ?? ''
    };
    const set = new Set(this.productosEditandoReview());
    set.add(productoId);
    this.productosEditandoReview.set(set);
    this.errorReview.set(null);
  }

  cerrarFormularioReview(productoId: number): void {
    const set = new Set(this.productosEditandoReview());
    set.delete(productoId);
    this.productosEditandoReview.set(set);
  }

  guardarReview(productoId: number, usuarioId: number): void {
    const formulario = this.formsReview[productoId];
    if (!formulario) return;

    this.guardandoReview.set(productoId);
    this.errorReview.set(null);

    const dto: ReviewRequest = {
      productoId,
      usuarioId,
      calificacion: Number(formulario.calificacion),
      comentario: formulario.comentario.trim()
    };

    const existente = this.reviewDe(productoId);
    const peticion = existente
      ? this.reviewsService.actualizar(existente.id, dto)
      : this.reviewsService.crear(dto);

    peticion.subscribe({
      next: (review) => {
        const mapa = new Map(this.reviewsPorProducto());
        mapa.set(productoId, review);
        this.reviewsPorProducto.set(mapa);
        this.guardandoReview.set(null);
        this.cerrarFormularioReview(productoId);
      },
      error: (err) => {
        this.guardandoReview.set(null);
        this.errorReview.set(`No se pudo guardar la review (error ${err.status})`);
      }
    });
  }
}