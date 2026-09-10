import { Component } from '@angular/core';

interface ContactChannel {
  label: string;
  handle: string;
  href: string;
  icon: 'instagram' | 'gmail' | 'group';
  accent: 'accent' | 'primary' | 'fire';
  external?: boolean;
}

interface InfoBlock {
  title: string;
  icon: 'tag' | 'truck' | 'clock';
  items: string[];
}

@Component({
  selector: 'app-contact',
  standalone: true,
  templateUrl: './contact.component.html',
  styleUrl: './contact.component.scss',
})
export class ContactComponent {
  channels: ContactChannel[] = [
    {
      label: 'Instagram',
      handle: '@dittostore.tcg',
      href: 'https://www.pokemon.com/us/pokemon-tcg/',
      icon: 'instagram',
      accent: 'accent',
      external: true,
    },
    {
      label: 'Correo',
      handle: 'contacto.dittostore@gmail.com',
      href: 'https://www.pokemon.com/us/pokemon-tcg/',
      icon: 'gmail',
      accent: 'primary',
      external: true,
    },
    {
      label: 'Grupo de la comunidad',
      handle: 'Únete y entérate de los drops',
      href: 'https://youtu.be/vfc42Pb5RA8?si=QjBPkzCU5WKPZPoC',
      icon: 'group',
      accent: 'fire',
      external: true,
    },
  ];

  infoBlocks: InfoBlock[] = [
    {
      title: 'Venta',
      icon: 'tag',
      items: [
        'Stock limitado por caída de producto, se publica el día y hora en Instagram.',
        'Reservas válidas por 24 horas con abono del 30%.',
        'Medios de pago: transferencia, Webpay y efectivo en retiro.',
      ],
    },
    {
      title: 'Despacho',
      icon: 'truck',
      items: [
        'Despacho a todo Chile por Chilexpress, 2 a 5 días hábiles.',
        'Retiro en persona sin costo, coordinado por Instagram o correo.',
        'Envío gratis en compras sobre $60.000.',
      ],
    },
    {
      title: 'Atención',
      icon: 'clock',
      items: [
        'Respuesta por Instagram o correo de lunes a sábado, 10:00 a 20:00.',
        'Cambios y devoluciones solo por producto sellado con defecto de fábrica.',
      ],
    },
  ];
}
