import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductoService, Producto } from '../core/services/producto.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [DecimalPipe, RouterLink],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home implements OnInit {
  private productoService = inject(ProductoService);
  private cdr = inject(ChangeDetectorRef);

  productosDestacados: Producto[] = [];

  private readonly trendingImages: string[] = [
    '/home/carta-snorlax.jpg',
    '/home/carta-eevee.png',
    '/home/carta-3.png',
    '/home/carta-4.png',
    '/home/carta-5.png',
    '/home/carta-6.jpg'
  ];

  // Se duplica la lista para que el desplazamiento automático sea continuo (sin salto al reiniciar).
  trendingImagesLoop: string[] = [...this.trendingImages, ...this.trendingImages];

  ngOnInit(): void {
    this.cargarProductos();
  }

  private cargarProductos(): void {
    this.productoService.listar().subscribe({
      next: (datos) => {
        this.productosDestacados = datos;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error al cargar los productos:', err);
      }
    });
  }
}