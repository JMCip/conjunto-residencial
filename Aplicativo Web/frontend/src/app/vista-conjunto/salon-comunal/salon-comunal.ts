import { Component, output } from '@angular/core';
import { Vista } from '../../models/vista.enum';
import { ModuloPendiente } from '../modulo-pendiente/modulo-pendiente';

/**
 * @component SalonComunal
 * @description Reservas del salón comunal.
 * Mientras no haya endpoint ni mock, muestra el módulo pendiente.
 */
@Component({
  selector: 'app-salon-comunal',
  imports: [ModuloPendiente],
  templateUrl: './salon-comunal.html',
  styleUrl: './salon-comunal.css',
})
export class SalonComunal {
  /** Pide al contenedor cambiar de sección. */
  readonly irA = output<Vista>();
}
