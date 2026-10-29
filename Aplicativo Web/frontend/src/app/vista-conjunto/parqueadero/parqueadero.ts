import { Component, output } from '@angular/core';
import { Vista } from '../../models/vista.enum';
import { ModuloPendiente } from '../modulo-pendiente/modulo-pendiente';

/**
 * @component Parqueadero
 * @description Parqueaderos del conjunto y su asignación.
 * Mientras no haya endpoint ni mock, muestra el módulo pendiente.
 */
@Component({
  selector: 'app-parqueadero',
  imports: [ModuloPendiente],
  templateUrl: './parqueadero.html',
  styleUrl: './parqueadero.css',
})
export class Parqueadero {
  /** Pide al contenedor cambiar de sección. */
  readonly irA = output<Vista>();
}
