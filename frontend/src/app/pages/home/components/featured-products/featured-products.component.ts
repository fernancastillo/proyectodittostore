import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProductCardComponent } from '../../../../shared/components/product-card/product-card.component';
import { PokemonTcgService } from '../../../../core/services/pokemon-tcg.service';
import { StoreProduct } from '../../../../core/models/card.model';

@Component({
  selector: 'app-featured-products',
  standalone: true,
  imports: [ProductCardComponent, RouterLink],
  templateUrl: './featured-products.component.html',
  styleUrl: './featured-products.component.scss',
})
export class FeaturedProductsComponent implements OnInit {
  private pokemonTcg = inject(PokemonTcgService);

  readonly products = signal<StoreProduct[]>([]);
  readonly loading = signal(true);
  readonly errored = signal(false);

  ngOnInit(): void {
    this.pokemonTcg.getFeaturedProducts(8).subscribe({
      next: (products) => {
        this.products.set(products);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errored.set(true);
      },
    });
  }
}
