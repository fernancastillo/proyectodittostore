import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, map, of, retry, shareReplay, timeout } from 'rxjs';
import { PokemonCard, PokemonSet, StoreProduct } from '../models/card.model';

interface PokemonTcgResponse {
  data: PokemonCard[];
}

interface PokemonTcgSetsResponse {
  data: PokemonSet[];
}

/** Tipos de caja que armamos a partir de cada set, con su rango de precio en CLP. */
const TIPOS_CAJA: { etiqueta: string; min: number; max: number; sobresPorCaja: number }[] = [
  { etiqueta: 'Booster Box', min: 65000, max: 95000, sobresPorCaja: 36 },
  { etiqueta: 'Elite Trainer Box', min: 38000, max: 48000, sobresPorCaja: 9 },
  { etiqueta: 'Booster Bundle', min: 18000, max: 24000, sobresPorCaja: 6 },
];

/** Cuánto tiempo máximo esperamos a la API antes de rendirnos (ms). */
const REQUEST_TIMEOUT_MS = 6000;
/** Reintentos ante fallas de red/timeout, con backoff simple. */
const RETRY_COUNT = 1;
const RETRY_DELAY_MS = 500;
/** TTL del cache en localStorage: evita repetir la espera al recargar o volver a navegar. */
const CACHE_TTL_MS = 10 * 60 * 1000;
const CACHE_PREFIX = 'ditto-cache:';

/**
 * Cartas de respaldo (Base Set, imágenes desde el CDN images.pokemontcg.io,
 * que es un servicio aparte del endpoint JSON de la API y es mucho más
 * estable). Se muestran si la API de datos falla o tarda demasiado, para que
 * el catálogo nunca se vea vacío o roto.
 */
const FALLBACK_CARDS: StoreProduct[] = [
  { id: 'base1-4', nombre: 'Charizard', precio: 45000, moneda: 'CLP', descripcion: 'Rareza Rare Holo del set Base. Uno de los clásicos más buscados por coleccionistas.', imagen: 'https://images.pokemontcg.io/base1/4_hires.png', rareza: 'Rare Holo', setNombre: 'Base' },
  { id: 'base1-2', nombre: 'Blastoise', precio: 28000, moneda: 'CLP', descripcion: 'Rareza Rare Holo del set Base.', imagen: 'https://images.pokemontcg.io/base1/2_hires.png', rareza: 'Rare Holo', setNombre: 'Base' },
  { id: 'base1-15', nombre: 'Venusaur', precio: 26000, moneda: 'CLP', descripcion: 'Rareza Rare Holo del set Base.', imagen: 'https://images.pokemontcg.io/base1/15_hires.png', rareza: 'Rare Holo', setNombre: 'Base' },
  { id: 'base1-10', nombre: 'Mewtwo', precio: 22000, moneda: 'CLP', descripcion: 'Rareza Rare Holo del set Base.', imagen: 'https://images.pokemontcg.io/base1/10_hires.png', rareza: 'Rare Holo', setNombre: 'Base' },
  { id: 'base1-8', nombre: 'Machamp', precio: 12000, moneda: 'CLP', descripcion: 'Rareza Rare Holo del set Base.', imagen: 'https://images.pokemontcg.io/base1/8_hires.png', rareza: 'Rare Holo', setNombre: 'Base' },
  { id: 'base1-6', nombre: 'Gyarados', precio: 14000, moneda: 'CLP', descripcion: 'Rareza Rare Holo del set Base.', imagen: 'https://images.pokemontcg.io/base1/6_hires.png', rareza: 'Rare Holo', setNombre: 'Base' },
  { id: 'base1-11', nombre: 'Nidoking', precio: 9000, moneda: 'CLP', descripcion: 'Rareza Rare Holo del set Base.', imagen: 'https://images.pokemontcg.io/base1/11_hires.png', rareza: 'Rare Holo', setNombre: 'Base' },
  { id: 'base1-58', nombre: 'Pikachu', precio: 3500, moneda: 'CLP', descripcion: 'Rareza Common del set Base. La mascota de la franquicia.', imagen: 'https://images.pokemontcg.io/base1/58_hires.png', rareza: 'Common', setNombre: 'Base' },
];

/** Sets reales con logo estable en el CDN, usados como respaldo para armar cajas. */
const FALLBACK_SETS: PokemonSet[] = [
  { id: 'sv1', name: 'Scarlet & Violet', series: 'Scarlet & Violet', releaseDate: '2023/03/31', total: 198, images: { symbol: '', logo: 'https://images.pokemontcg.io/sv1/logo.png' } },
  { id: 'swsh12', name: 'Silver Tempest', series: 'Sword & Shield', releaseDate: '2022/11/11', total: 195, images: { symbol: '', logo: 'https://images.pokemontcg.io/swsh12/logo.png' } },
  { id: 'swsh45', name: 'Shining Fates', series: 'Sword & Shield', releaseDate: '2021/02/19', total: 73, images: { symbol: '', logo: 'https://images.pokemontcg.io/swsh45/logo.png' } },
  { id: 'base5', name: 'Team Rocket', series: 'Base', releaseDate: '2000/04/24', total: 83, images: { symbol: '', logo: 'https://images.pokemontcg.io/base5/logo.png' } },
  { id: 'gym1', name: 'Gym Heroes', series: 'Gym', releaseDate: '2000/08/14', total: 132, images: { symbol: '', logo: 'https://images.pokemontcg.io/gym1/logo.png' } },
  { id: 'neo1', name: 'Neo Genesis', series: 'Neo', releaseDate: '2000/12/16', total: 111, images: { symbol: '', logo: 'https://images.pokemontcg.io/neo1/logo.png' } },
];

/**
 * Consume la Pokémon TCG API (https://pokemontcg.io) para obtener imágenes,
 * nombres y datos reales de cartas.
 *
 * La API pública (sin API key) es poco confiable en la práctica (alta tasa de
 * error, respuestas lentas), así que este service:
 *  - aplica timeout + 1 reintento a cada request,
 *  - cachea en memoria (shareReplay) para que dos componentes que piden lo
 *    mismo en paralelo no dupliquen la llamada,
 *  - cachea en localStorage por unos minutos para que volver a una página no
 *    implique esperar de nuevo,
 *  - si aun así falla, cae a un catálogo de respaldo (FALLBACK_*) con
 *    imágenes reales servidas desde el CDN estático de imágenes, para que el
 *    catálogo nunca se vea vacío o roto.
 *
 * IMPORTANTE: el precio se calcula acá solo como valor de referencia visual
 * (a partir del precio de mercado en USD que a veces trae tcgplayer, o de la
 * rareza como respaldo). Cuando el `producto-service` del backend exponga
 * precio y stock reales, este service debería consumir ese endpoint en su
 * lugar y dejar de inventar precios.
 */
@Injectable({ providedIn: 'root' })
export class PokemonTcgService {
  private http = inject(HttpClient);
  private readonly baseUrl = 'https://api.pokemontcg.io/v2/cards';
  private readonly usdToClpAprox = 950;

  /** Cache en memoria de la sesión actual (por URL), para no duplicar requests en vuelo. */
  private readonly memoryCache = new Map<string, Observable<StoreProduct[]>>();

  /** Cartas para la grilla de productos destacados. */
  getFeaturedProducts(pageSize = 8): Observable<StoreProduct[]> {
    const params = new URLSearchParams({
      q: 'supertype:pokemon rarity:"Rare Holo"',
      pageSize: String(pageSize),
      orderBy: '-set.releaseDate',
    });
    const url = `${this.baseUrl}?${params.toString()}`;

    return this.cachedRequest(
      url,
      () =>
        this.http
          .get<PokemonTcgResponse>(url)
          .pipe(map((res) => res.data.map((card) => this.toStoreProduct(card)))),
      () => FALLBACK_CARDS.slice(0, pageSize),
    );
  }

  getHeroCard(page = 1): Observable<StoreProduct | null> {
    const params = new URLSearchParams({
      q: 'supertype:pokemon',
      page: String(page),
      pageSize: '1',
      orderBy: '-set.releaseDate',
    });
    const url = `${this.baseUrl}?${params.toString()}`;

    return this.http.get<PokemonTcgResponse>(url).pipe(
      timeout(REQUEST_TIMEOUT_MS),
      retry({ count: RETRY_COUNT, delay: RETRY_DELAY_MS }),
      map((res) => (res.data.length ? this.toStoreProduct(res.data[0]) : null)),
      catchError(() => of(FALLBACK_CARDS[0] ?? null)),
    );
  }

  /**
   * Cajas de sobres (Booster Box, Elite Trainer Box, Booster Bundle) armadas a
   * partir de los sets reales de la Pokémon TCG API. El precio y el tipo de
   * caja son referenciales, ya que la API no vende cajas selladas, solo cartas
   * y sets.
   */
  getBoosterBoxes(pageSize = 8): Observable<StoreProduct[]> {
    const params = new URLSearchParams({
      pageSize: String(pageSize),
      orderBy: '-releaseDate',
    });
    const url = `https://api.pokemontcg.io/v2/sets?${params.toString()}`;

    return this.cachedRequest(
      url,
      () =>
        this.http.get<PokemonTcgSetsResponse>(url).pipe(
          map((res) =>
            res.data.filter((set) => !!set.images?.logo).map((set, i) => this.toBoxProduct(set, i)),
          ),
        ),
      () => FALLBACK_SETS.slice(0, pageSize).map((set, i) => this.toBoxProduct(set, i)),
    );
  }

  /**
   * Envuelve un request con timeout + retry + cache en memoria (shareReplay),
   * cache en localStorage (con TTL) y un catálogo de respaldo si todo lo
   * anterior falla, para que la UI nunca se quede sin nada que mostrar.
   */
  private cachedRequest(
    url: string,
    factory: () => Observable<StoreProduct[]>,
    fallback: () => StoreProduct[],
  ): Observable<StoreProduct[]> {
    const inFlight = this.memoryCache.get(url);
    if (inFlight) return inFlight;

    const fromStorage = this.readFromLocalStorage(url);
    if (fromStorage) {
      const cached$ = of(fromStorage);
      this.memoryCache.set(url, cached$);
      return cached$;
    }

    const request$ = factory().pipe(
      timeout(REQUEST_TIMEOUT_MS),
      retry({ count: RETRY_COUNT, delay: RETRY_DELAY_MS }),
      map((products) => {
        this.writeToLocalStorage(url, products);
        return products;
      }),
      catchError(() => {
        // No cacheamos el resultado de respaldo en localStorage: en la
        // próxima visita queremos volver a intentar la API real.
        this.memoryCache.delete(url);
        return of(fallback());
      }),
      shareReplay({ bufferSize: 1, refCount: false }),
    );

    this.memoryCache.set(url, request$);
    return request$;
  }

  private readFromLocalStorage(url: string): StoreProduct[] | null {
    try {
      const raw = localStorage.getItem(CACHE_PREFIX + url);
      if (!raw) return null;
      const { timestamp, data } = JSON.parse(raw) as { timestamp: number; data: StoreProduct[] };
      if (Date.now() - timestamp > CACHE_TTL_MS) {
        localStorage.removeItem(CACHE_PREFIX + url);
        return null;
      }
      return data;
    } catch {
      return null;
    }
  }

  private writeToLocalStorage(url: string, data: StoreProduct[]): void {
    try {
      localStorage.setItem(CACHE_PREFIX + url, JSON.stringify({ timestamp: Date.now(), data }));
    } catch {
      // Si el storage está lleno o bloqueado (modo privado, etc.), seguimos sin cache persistente.
    }
  }

  private toBoxProduct(set: PokemonSet, index: number): StoreProduct {
    const tipo = TIPOS_CAJA[index % TIPOS_CAJA.length];
    const precio = this.priceInStep(tipo.min, tipo.max, set.id);

    return {
      id: `box-${set.id}-${tipo.etiqueta.toLowerCase().replace(/\s+/g, '-')}`,
      nombre: `${tipo.etiqueta} — ${set.name}`,
      precio,
      moneda: 'CLP',
      descripcion: `Caja sellada del set ${set.name} (${set.series}). Incluye ${tipo.sobresPorCaja} sobres con cartas al azar, directo de fábrica.`,
      imagen: set.images.logo,
      rareza: tipo.etiqueta,
      setNombre: set.name,
      tipo: 'caja',
    };
  }

  /** Genera un precio pseudo-determinista dentro de un rango a partir del id del set. */
  private priceInStep(min: number, max: number, seed: string): number {
    const hash = Array.from(seed).reduce((acc, ch) => acc + ch.charCodeAt(0), 0);
    const span = max - min;
    const raw = min + (hash % 101) / 100 * span;
    return Math.round(raw / 500) * 500;
  }

  private toStoreProduct(card: PokemonCard): StoreProduct {
    return {
      id: card.id,
      nombre: card.name,
      precio: this.estimatePrice(card),
      moneda: 'CLP',
      descripcion: this.buildDescription(card),
      imagen: card.images.large ?? card.images.small,
      rareza: card.rarity,
      setNombre: card.set?.name,
    };
  }

  private buildDescription(card: PokemonCard): string {
    if (card.flavorText) return card.flavorText;
    const partes = [
      card.rarity ? `Rareza ${card.rarity}` : null,
      card.set?.name ? `del set ${card.set.name}` : null,
      card.types?.length ? `· tipo ${card.types.join('/')}` : null,
    ].filter(Boolean);
    return partes.length ? partes.join(' ') : 'Carta coleccionable Pokémon TCG.';
  }

  private estimatePrice(card: PokemonCard): number {
    const marketUsd = card.tcgplayer?.prices
      ? Object.values(card.tcgplayer.prices)[0]?.market
      : undefined;

    if (marketUsd) {
      return Math.round((marketUsd * this.usdToClpAprox) / 100) * 100;
    }

    const rarezaBase: Record<string, number> = {
      Common: 1500,
      Uncommon: 2500,
      Rare: 4500,
      'Rare Holo': 8000,
      'Rare Ultra': 25000,
      'Rare Holo VMAX': 32000,
      'Rare Secret': 45000,
    };
    return rarezaBase[card.rarity ?? ''] ?? 5000;
  }
}
