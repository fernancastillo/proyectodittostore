import {
  Component,
  OnDestroy,
  OnInit,
  inject,
  signal
} from '@angular/core';

import { RouterLink } from '@angular/router';
import { CurrencyPipe } from '@angular/common';
import { Subscription, interval } from 'rxjs';

import { PokemonTcgService } from '../../../../core/services/pokemon-tcg.service';
import { StoreProduct } from '../../../../core/models/card.model';

@Component({
  selector: 'app-hero',
  standalone: true,
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './hero.component.html',
  styleUrl: './hero.component.scss',
})
export class HeroComponent implements OnInit, OnDestroy {

  private pokemonTcg = inject(PokemonTcgService);

  readonly heroCard = signal<StoreProduct | null>(null);
  readonly loading = signal(true);

  /** true durante el breve instante del flip, para disparar la animación por CSS */
  readonly flipping = signal(false);

  readonly quickProducts = signal<StoreProduct[]>([]);
  readonly quickLoading = signal(true);

  private currentPage = 1;
  private rotationSubscription?: Subscription;
  private flipTimeout?: ReturnType<typeof setTimeout>;

  ngOnInit(): void {
    this.cargarCarta();
    this.cargarProductosRapidos();

    // Antes cambiaba cada 1.5s combinado con un giro infinito: quedaba muy
    // agitado. Ahora la carta descansa varios segundos y solo gira una vez
    // (flip) al cambiar, como si la estuvieran mostrando en vitrina.
    this.rotationSubscription = interval(1050).subscribe(() => {
      this.currentPage++;

      if (this.currentPage > 10) {
        this.currentPage = 1;
      }

      this.cargarCarta();
    });
  }

  private cargarCarta(): void {
    this.pokemonTcg.getHeroCard(this.currentPage).subscribe({
      next: (card) => {
        if (card) {
          this.dispararFlip();
          this.heroCard.set(card);
        }

        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error cargando Pokémon:', error);
        this.loading.set(false);
      },
    });
  }

  private dispararFlip(): void {
    clearTimeout(this.flipTimeout);
    this.flipping.set(true);
    this.flipTimeout = setTimeout(() => this.flipping.set(false), 700);
  }

  private cargarProductosRapidos(): void {
    this.pokemonTcg.getFeaturedProducts(10).subscribe({
      next: (products) => {
        this.quickProducts.set(products);
        this.quickLoading.set(false);
      },
      error: () => {
        this.quickLoading.set(false);
      },
    });
  }

  ngOnDestroy(): void {
    this.rotationSubscription?.unsubscribe();
    clearTimeout(this.flipTimeout);
  }
}