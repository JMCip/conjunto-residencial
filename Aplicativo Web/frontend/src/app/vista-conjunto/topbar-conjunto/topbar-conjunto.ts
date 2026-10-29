import { Component, computed, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { NOMBRES_VISTA, Vista } from '../../models/vista.enum';

/**
 * @component TopbarConjunto
 * @description Barra superior: migas de pan de la sección actual, chip "Solo consulta"
 * y la fecha de hoy en español.
 */
@Component({
  selector: 'app-topbar-conjunto',
  imports: [DatePipe],
  templateUrl: './topbar-conjunto.html',
  styleUrl: './topbar-conjunto.css',
})
export class TopbarConjunto {
  /** Sección que se está viendo. */
  readonly vista = input.required<Vista>();

  /** Nombre visible de la sección actual para las migas. */
  protected readonly nombreVista = computed(() => NOMBRES_VISTA[this.vista()]);

  /** Fecha de hoy (se toma al abrir la app). */
  protected readonly hoy = signal(new Date());
}
