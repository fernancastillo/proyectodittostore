# Ditto Store — Frontend (Angular 18, standalone)

Frontend de la tienda de cartas Pokémon TCG. Landing (`/`) completa; `/producto`,
`/carrito`, `/cuenta` y `/contacto` están como páginas "en construcción" listas
para reemplazar por las pantallas reales.

## Cómo correrlo

```bash
cd frontend
npm install
npm start        # http://localhost:4200
```

No se necesita API key: se consume la [Pokémon TCG API](https://docs.pokemontcg.io/)
pública para imágenes, nombres, sets y rareza de las cartas (`core/services/pokemon-tcg.service.ts`).
Tiene un límite de 1000 requests/día sin key, de sobra para desarrollo. Si en algún
momento da error 429/CORS, se puede sacar una key gratis en pokemontcg.io y mandarla
en el header `X-Api-Key`.

## Estructura

```
src/app/
  core/
    models/card.model.ts        # tipos de carta (API) y de producto (tienda)
    services/pokemon-tcg.service.ts
  shared/components/
    navbar/                     # logo + links + buscador/carrito/cuenta
    footer/                     # ola + carita de Ditto + columnas
    product-card/               # imagen (click = descripción), nombre, precio
  pages/
    home/
      components/hero/          # carta 3D girando + copy
      components/categories/    # 3 categorías (sobres, sueltas, colecciones)
      components/featured-products/
    coming-soon/                 # placeholder reutilizable para producto/carrito/contacto/cuenta
```

## Decisiones de diseño

- **Paleta**: lavanda `#8B6CE0` / `#CDBDF5` como marca, rosa `#F2A0C6` de acento
  (mejillas de Ditto) y dorado `#E7B84B` para reservar a piezas premium
  (badges de rareza alta). Fondo casi blanco, nunca gris puro.
- **Tipografía**: `Baloo 2` para títulos (redonda, juguetona, va con el tono
  "tienda de cartas coleccionables") e `Inter` para todo el texto de cuerpo/UI.
- **El momento fuerte** del diseño es la carta girando en 3D del hero — el
  resto de la página se mantiene simple a propósito para no competirle.
- El marco redondeado lavanda alrededor de toda la app (visible en pantallas
  ≥900px) retoma el borde de tu boceto original.

## Precios: importante

`pokemon-tcg.service.ts` **inventa** un precio en CLP (a partir del precio de
mercado en USD de TCGPlayer cuando la API lo trae, o de la rareza como
respaldo) solo para poder mostrar algo en las tarjetas. El backend ya tiene un
`producto-service` — cuando esté disponible su endpoint de catálogo/precio,
hay que:

1. Cambiar `getFeaturedProducts()` para llamar a ese endpoint (vía el API
   Gateway / BFF) en vez de a la Pokémon TCG API directamente, o
2. Dejar la Pokémon TCG API solo para imagen/nombre/descripción y cruzarla
   con el precio/stock real que devuelva el backend por SKU.

## Pendiente para las próximas pantallas

- `/producto`: listado completo + filtros (tipo, rareza, set) — se puede
  reusar `ProductCardComponent` y ampliar `PokemonTcgService` con paginación.
- `/carrito`, `/cuenta`: necesitan estado compartido (un `CartService` con
  signals sería suficiente para partir) y conectar con `carrito-service` /
  `usuarios-service` del backend.
- `/contacto`: formulario simple con `ReactiveForms`.
- Reemplazar el ícono de Ditto del navbar/logo si tienes uno propio: solo hay
  que cambiar la URL en `navbar.component.html` por tu archivo en
  `src/assets/images/`.
