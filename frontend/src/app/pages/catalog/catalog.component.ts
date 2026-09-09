import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { ProductCardComponent } from '../../shared/components/product-card/product-card.component';
import { PokemonTcgService } from '../../core/services/pokemon-tcg.service';
import { StoreProduct } from '../../core/models/card.model';

type Filtro = 'todos' | 'carta' | 'caja';
type Orden = 'relevancia' | 'precio-asc' | 'precio-desc' | 'nombre';

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [ProductCardComponent, FormsModule],
  templateUrl: './catalog.component.html',
  styleUrl: './catalog.component.scss',
})
export class CatalogComponent implements OnInit {
  private pokemonTcg = inject(PokemonTcgService);

  readonly loading = signal(true);
  readonly errored = signal(false);
  readonly filtro = signal<Filtro>('todos');
  readonly orden = signal<Orden>('relevancia');
  readonly busqueda = signal('');
  private readonly allProducts = signal<StoreProduct[]>([]);

  /** Rarezas disponibles en los datos ya cargados, para armar los chips dinámicamente. */
  readonly rarezasDisponibles = computed(() => {
    const set = new Set<string>();
    this.allProducts().forEach((p) => {
      if (p.rareza) set.add(p.rareza);
    });
    return Array.from(set).sort();
  });

  readonly rareza = signal<string | null>(null);

  readonly products = computed(() => {
    let items = this.allProducts();

    const filtro = this.filtro();
    if (filtro !== 'todos') {
      items = items.filter((p) => (p.tipo ?? 'carta') === filtro);
    }

    const rareza = this.rareza();
    if (rareza) {
      items = items.filter((p) => p.rareza === rareza);
    }

    const query = this.busqueda().trim().toLowerCase();
    if (query) {
      items = items.filter(
        (p) =>
          p.nombre.toLowerCase().includes(query) ||
          (p.setNombre ?? '').toLowerCase().includes(query),
      );
    }

    const orden = this.orden();
    items = [...items];
    if (orden === 'precio-asc') items.sort((a, b) => a.precio - b.precio);
    if (orden === 'precio-desc') items.sort((a, b) => b.precio - a.precio);
    if (orden === 'nombre') items.sort((a, b) => a.nombre.localeCompare(b.nombre));

    return items;
  });

  readonly filtros: { valor: Filtro; label: string }[] = [
    { valor: 'todos', label: 'Todos' },
    { valor: 'carta', label: 'Cartas' },
    { valor: 'caja', label: 'Cajas' },
  ];

  readonly ordenes: { valor: Orden; label: string }[] = [
    { valor: 'relevancia', label: 'Relevancia' },
    { valor: 'precio-asc', label: 'Precio: menor a mayor' },
    { valor: 'precio-desc', label: 'Precio: mayor a menor' },
    { valor: 'nombre', label: 'Nombre (A-Z)' },
  ];

  ngOnInit(): void {
    forkJoin([this.pokemonTcg.getFeaturedProducts(12), this.pokemonTcg.getBoosterBoxes(12)]).subscribe({
      next: ([cartas, cajas]) => {
        this.allProducts.set([...cartas, ...cajas]);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errored.set(true);
      },
    });
  }

  setFiltro(valor: Filtro): void {
    this.filtro.set(valor);
    this.rareza.set(null);
  }

  setRareza(valor: string): void {
    this.rareza.update((current) => (current === valor ? null : valor));
  }

  setOrden(valor: Orden): void {
    this.orden.set(valor);
  }

  setBusqueda(valor: string): void {
    this.busqueda.set(valor);
  }

  limpiarFiltros(): void {
    this.filtro.set('todos');
    this.rareza.set(null);
    this.busqueda.set('');
    this.orden.set('relevancia');
  }
}
