import { Component, Input, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { StoreProduct } from '../../../core/models/card.model';
import { CartService } from '../../../core/services/cart.service';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.scss',
})
export class ProductCardComponent {
  @Input({ required: true }) product!: StoreProduct;

  private cart = inject(CartService);

  readonly showDescription = signal(false);
  readonly recienAgregado = signal(false);

  toggleDescription(): void {
    this.showDescription.update((show) => !show);
  }

  agregarAlCarrito(): void {
    this.cart.agregar(this.product);
    this.recienAgregado.set(true);
    setTimeout(() => this.recienAgregado.set(false), 1200);
  }
}