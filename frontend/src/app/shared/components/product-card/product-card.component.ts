import { Component, Input, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { StoreProduct } from '../../../core/models/card.model';

@Component({
  selector: 'app-product-card',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './product-card.component.html',
  styleUrl: './product-card.component.scss',
})
export class ProductCardComponent {
  @Input({ required: true }) product!: StoreProduct;

  readonly showDescription = signal(false);

  toggleDescription(): void {
    this.showDescription.update((show) => !show);
  }
}
