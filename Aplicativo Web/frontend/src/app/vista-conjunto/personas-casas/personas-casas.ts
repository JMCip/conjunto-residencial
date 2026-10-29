import { Component, output } from '@angular/core';
import { Vista } from '../../models/vista.enum';
import { ModuloPendiente } from '../modulo-pendiente/modulo-pendiente';

/**
 * @component PersonasCasas
 * @description Propietarios, arrendatarios, residentes, visitantes y casas del conjunto.
 * Mientras no haya endpoint ni mock, muestra el módulo pendiente.
 */
@Component({
  selector: 'app-personas-casas',
  imports: [ModuloPendiente],
  templateUrl: './personas-casas.html',
  styleUrl: './personas-casas.css',
})
export class PersonasCasas {
  /** Pide al contenedor cambiar de sección. */
  readonly irA = output<Vista>();
}
