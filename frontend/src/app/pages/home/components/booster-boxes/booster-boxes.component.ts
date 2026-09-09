import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProductCardComponent } from '../../../../shared/components/product-card/product-card.component';
import { PokemonTcgService } from '../../../../core/services/pokemon-tcg.service';
import { StoreProduct } from '../../../../core/models/card.model';

@Component({
  selector: 'app-booster-boxes',
  standalone: true,
  imports: [ProductCardComponent, RouterLink],
  templateUrl: './booster-boxes.component.html',
  styleUrl: './booster-boxes.component.scss',
})
export class BoosterBoxesComponent implements OnInit {
  private pokemonTcg = inject(PokemonTcgService);

  readonly boxes = signal<StoreProduct[]>([]);
  readonly loading = signal(true);
  readonly errored = signal(false);

  ngOnInit(): void {
    this.pokemonTcg.getBoosterBoxes(4).subscribe({
      next: (boxes) => {
        this.boxes.set(boxes);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errored.set(true);
      },
    });
  }
}
