import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-coming-soon',
  standalone: true,
  template: `
    <section class="coming-soon">
      <span class="coming-soon__blob" aria-hidden="true"></span>
      <h1>{{ title }}</h1>
      <p>Esta sección se está preparando. Muy pronto vas a poder verla completa aquí.</p>
    </section>
  `,
  styles: [
    `
      .coming-soon {
        min-height: 50vh;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        text-align: center;
        gap: 0.75rem;
        padding: 4rem 1.5rem;
        position: relative;
      }
      .coming-soon__blob {
        position: absolute;
        inset: 0;
        margin: auto;
        width: 220px;
        height: 220px;
        background: radial-gradient(circle, var(--color-primary-light) 0%, transparent 70%);
        opacity: 0.6;
        z-index: -1;
      }
      h1 {
        font-family: var(--font-display);
        font-size: clamp(1.75rem, 4vw, 2.5rem);
        color: var(--color-ink);
        margin: 0;
      }
      p {
        color: var(--color-ink-soft);
        max-width: 32ch;
      }
    `,
  ],
})
export class ComingSoonComponent {
  private route = inject(ActivatedRoute);
  title = this.route.snapshot.data['title'] ?? 'Próximamente';
}
