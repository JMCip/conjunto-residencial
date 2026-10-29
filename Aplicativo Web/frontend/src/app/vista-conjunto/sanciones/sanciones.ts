import { Component, output } from '@angular/core';
import { Vista } from '../../models/vista.enum';
import { ModuloPendiente } from '../modulo-pendiente/modulo-pendiente';

/**
 * @component Sanciones
 * @description Sanciones a casas y residentes.
 * Mientras no haya endpoint ni mock, muestra el módulo pendiente.
 */
@Component({
  selector: 'app-sanciones',
  imports: [ModuloPendiente],
  templateUrl: './sanciones.html',
  styleUrl: './sanciones.css',
})
export class Sanciones {
  /** Pide al contenedor cambiar de sección. */
  readonly irA = output<Vista>();
}
