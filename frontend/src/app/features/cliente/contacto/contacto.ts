import { Component } from '@angular/core';

@Component({
  selector: 'app-contacto',
  standalone: true,
  template: `
    <section class="featured-section">
      <h3 class="section-title">Contacto</h3>
      <div style="max-width: 600px; margin: 0 auto; text-align: center;">
        <p>¿Tienes dudas o comentarios? Escríbenos a <strong>contacto&#64;dittostore.com</strong>.</p>
      </div>
    </section>
  `
})
export class Contacto {}