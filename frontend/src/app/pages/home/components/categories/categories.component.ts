import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

interface Category {
  title: string;
  description: string;
  logo: string;
}

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './categories.component.html',
  styleUrl: './categories.component.scss',
})
export class CategoriesComponent {
  readonly categories: Category[] = [
    {
      title: 'Sobres y boosters',
      description: 'Sella la suerte: sobres de las últimas expansiones, sellados de fábrica.',
      logo: 'https://images.pokemontcg.io/sv1/logo.png',
    },
    {
      title: 'Cartas individuales',
      description: 'Busca justo la carta que te falta para completar tu colección.',
      logo: 'https://images.pokemontcg.io/swsh12/logo.png',
    },
    {
      title: 'Colecciones especiales',
      description: 'Cajas y sets premium con piezas exclusivas y arte alternativo.',
      logo: 'https://images.pokemontcg.io/swsh45/logo.png',
    },
  ];
}
