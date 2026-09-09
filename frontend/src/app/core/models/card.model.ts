/**
 * Subconjunto de campos que usamos de la respuesta de la Pokémon TCG API
 * (https://docs.pokemontcg.io/). Se dejan opcionales los campos que no
 * todas las cartas traen.
 */
export interface PokemonCardImages {
  small: string;
  large: string;
}

export interface PokemonCardSet {
  id: string;
  name: string;
  series: string;
  images: { symbol: string; logo: string };
}

export interface PokemonCardPriceRange {
  low?: number;
  mid?: number;
  high?: number;
  market?: number;
}

export interface PokemonCardTcgPlayer {
  url?: string;
  prices?: Record<string, PokemonCardPriceRange>;
}

export interface PokemonCard {
  id: string;
  name: string;
  supertype: string;
  subtypes?: string[];
  hp?: string;
  types?: string[];
  rarity?: string;
  flavorText?: string;
  images: PokemonCardImages;
  set: PokemonCardSet;
  tcgplayer?: PokemonCardTcgPlayer;
}

export interface PokemonSet {
  id: string;
  name: string;
  series: string;
  releaseDate: string;
  total: number;
  images: { symbol: string; logo: string };
}

/** Forma ya lista para pintar una carta como producto en la tienda. */
export interface StoreProduct {
  id: string;
  nombre: string;
  precio: number;
  moneda: 'CLP';
  descripcion: string;
  imagen: string;
  rareza?: string;
  setNombre?: string;
  /** Tipo de producto: carta individual o caja/sobre. Por defecto 'carta'. */
  tipo?: 'carta' | 'caja';
}
