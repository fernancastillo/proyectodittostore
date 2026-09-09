import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  ViewChild,
  inject,
  signal,
} from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { OrderService, Pedido, PedidoItem } from '../../core/services/order.service';

// Cliente de sandbox público de PayPal, solo para poder renderizar el botón
// en modo demo. Para producción hay que reemplazarlo por el Client ID real
// del comercio (y lo ideal es capturar la orden desde el backend, no desde
// el navegador).
const PAYPAL_CLIENT_ID = 'sb';

declare global {
  interface Window {
    paypal?: any;
  }
}

type MetodoUI = 'paypal' | 'tarjeta';

const ENVIO_ESTANDAR = 3990;
const ENVIO_GRATIS_DESDE = 60000;

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CurrencyPipe, FormsModule, RouterLink],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.scss',
})
export class CheckoutComponent implements AfterViewInit, OnDestroy {
  readonly cart = inject(CartService);
  private orders = inject(OrderService);
  private router = inject(Router);

  @ViewChild('paypalContainer') paypalContainer?: ElementRef<HTMLDivElement>;

  readonly metodo = signal<MetodoUI>('paypal');
  readonly paypalListo = signal(false);
  readonly paypalError = signal(false);
  readonly procesando = signal(false);
  readonly pedidoConfirmado = signal<Pedido | null>(null);

  readonly tarjeta = {
    numero: '',
    nombre: '',
    vencimiento: '',
    cvv: '',
  };

  get items() {
    return this.cart.items$();
  }

  get subtotal(): number {
    return this.cart.subtotal();
  }

  get envio(): number {
    if (this.subtotal >= ENVIO_GRATIS_DESDE) return 0;
    return ENVIO_ESTANDAR;
  }

  get total(): number {
    return this.subtotal + this.envio;
  }

  ngAfterViewInit(): void {
    if (this.cart.estaVacio()) return;
    this.cargarBotonesPaypal();
  }

  ngOnDestroy(): void {
    // Limpia el contenedor del botón para evitar renders duplicados si el
    // componente se vuelve a montar (por ejemplo al navegar de ida y vuelta).
    if (this.paypalContainer) {
      this.paypalContainer.nativeElement.innerHTML = '';
    }
  }

  cambiarMetodo(metodo: MetodoUI): void {
    this.metodo.set(metodo);
    if (metodo === 'paypal') {
      setTimeout(() => this.cargarBotonesPaypal(), 0);
    }
  }

  /** Carga el SDK de PayPal (una sola vez) y renderiza los botones. */
  private cargarBotonesPaypal(): void {
    if (!this.paypalContainer) return;

    this.paypalContainer.nativeElement.innerHTML = '';

    this.obtenerSdkPaypal()
      .then((paypal) => {
        this.paypalListo.set(true);
        paypal
          .Buttons({
            style: { color: 'blue', shape: 'pill', label: 'paypal', height: 45 },
            createOrder: (_data: unknown, actions: any) =>
              actions.order.create({
                purchase_units: [
                  {
                    amount: {
                      currency_code: 'USD',
                      // Demo: se convierte a un monto simbólico en USD porque
                      // el sandbox de prueba no siempre acepta CLP.
                      value: Math.max(1, Math.round(this.total / 900)).toFixed(2),
                    },
                  },
                ],
              }),
            onApprove: (_data: unknown, actions: any) =>
              actions.order.capture().then(() => this.confirmarPedido('paypal')),
            onError: () => this.paypalError.set(true),
          })
          .render(this.paypalContainer!.nativeElement);
      })
      .catch(() => this.paypalError.set(true));
  }

  private obtenerSdkPaypal(): Promise<any> {
    if (window.paypal) return Promise.resolve(window.paypal);

    return new Promise((resolve, reject) => {
      const existente = document.getElementById('paypal-sdk');
      if (existente) {
        existente.addEventListener('load', () => resolve(window.paypal));
        existente.addEventListener('error', reject);
        return;
      }

      const script = document.createElement('script');
      script.id = 'paypal-sdk';
      script.src = `https://www.paypal.com/sdk/js?client-id=${PAYPAL_CLIENT_ID}&currency=USD`;
      script.onload = () => resolve(window.paypal);
      script.onerror = reject;
      document.body.appendChild(script);
    });
  }

  pagarConTarjetaDitto(): void {
    if (
      this.tarjeta.numero.replace(/\s/g, '').length < 16 ||
      !this.tarjeta.nombre.trim() ||
      !this.tarjeta.vencimiento.trim() ||
      this.tarjeta.cvv.trim().length < 3
    ) {
      return;
    }

    this.procesando.set(true);
    // Simula la latencia de una pasarela de pago real.
    setTimeout(() => {
      this.procesando.set(false);
      this.confirmarPedido('tarjeta-ditto');
    }, 1400);
  }

  private confirmarPedido(metodoPago: 'paypal' | 'tarjeta-ditto'): void {
    const items: PedidoItem[] = this.items.map((i) => ({
      productId: i.product.id,
      nombre: i.product.nombre,
      imagen: i.product.imagen,
      cantidad: i.cantidad,
      precioUnitario: i.product.precio,
    }));

    const pedido = this.orders.crearPedido({
      items,
      subtotal: this.subtotal,
      envio: this.envio,
      total: this.total,
      metodoPago,
    });

    this.cart.vaciar();
    this.pedidoConfirmado.set(pedido);
  }

  volverAlInicio(): void {
    this.router.navigateByUrl('/');
  }
}