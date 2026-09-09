import { Component, OnInit, inject, signal } from '@angular/core';
import { ProductCardComponent } from '../../shared/components/product-card/product-card.component';
import { PokemonTcgService } from '../../core/services/pokemon-tcg.service';
import { StoreProduct } from '../../core/models/card.model';

@Component({
  selector: 'app-boxes',
  standalone: true,
  imports: [ProductCardComponent],
  templateUrl: './boxes.component.html',
  styleUrl: './boxes.component.scss',
})
export class BoxesComponent implements OnInit {
  private pokemonTcg = inject(PokemonTcgService);

  readonly boxes = signal<StoreProduct[]>([]);
  readonly loading = signal(true);
  readonly errored = signal(false);

  ngOnInit(): void {
    this.pokemonTcg.getBoosterBoxes(16).subscribe({
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
