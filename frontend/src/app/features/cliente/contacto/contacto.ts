import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-contacto',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './contacto.html',
  styleUrl: './contacto.scss'
})
export class Contacto {
  private cdr = inject(ChangeDetectorRef);

  nombre = '';
  email = '';
  asunto = 'Consulta general';
  mensaje = '';

  enviando = false;
  enviado = false;
  error: string | null = null;

  private esEmailValido(correo: string): boolean {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(correo.trim());
  }

  enviarFormulario(): void {
    if (!this.nombre.trim() || !this.email.trim() || !this.mensaje.trim()) {
      this.error = 'Por favor, completa todos los campos obligatorios (*).';
      return;
    }

    if (!this.esEmailValido(this.email)) {
      this.error = 'Por favor, ingresa un correo electrónico válido (debe incluir "@" y un dominio correcto).';
      return;
    }

    this.error = null;
    this.enviando = true;

    setTimeout(() => {
      this.enviando = false;
      this.enviado = true;
      this.cdr.detectChanges();
    }, 1500);
  }

  reiniciarFormulario(): void {
    this.nombre = '';
    this.email = '';
    this.asunto = 'Consulta general';
    this.mensaje = '';
    this.enviado = false;
    this.error = null;
    this.cdr.detectChanges();
  }
}