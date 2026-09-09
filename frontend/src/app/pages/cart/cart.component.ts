import { Component, inject } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CartService } from '../../core/services/cart.service';

const ENVIO_ESTANDAR = 3990;
const ENVIO_GRATIS_DESDE = 60000;

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.scss',
})
export class CartComponent {
  readonly cart = inject(CartService);

  readonly envioGratisDesde = ENVIO_GRATIS_DESDE;

  get envio(): number {
    if (this.cart.estaVacio() || this.cart.subtotal() >= ENVIO_GRATIS_DESDE) return 0;
    return ENVIO_ESTANDAR;
  }

  get total(): number {
    return this.cart.subtotal() + this.envio;
  }

  get faltaParaEnvioGratis(): number {
    return Math.max(0, ENVIO_GRATIS_DESDE - this.cart.subtotal());
  }

  incrementar(id: string): void {
    this.cart.incrementar(id);
  }

  decrementar(id: string): void {
    this.cart.decrementar(id);
  }

  eliminar(id: string): void {
    this.cart.eliminar(id);
  }

  vaciarCarrito(): void {
    this.cart.vaciar();
  }
}