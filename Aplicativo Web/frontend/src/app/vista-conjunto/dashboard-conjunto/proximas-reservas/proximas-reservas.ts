import { Component, input, output } from '@angular/core';
import { DatePipe, SlicePipe } from '@angular/common';
import { ReservaSalonModel } from '../../../models/reserva-salon.model';

/**
 * @component ProximasReservas
 * @description Tarjeta del Inicio con las próximas reservas del salón comunal.
 * Si no hay reservas desde hoy, muestra el estado vacío.
 */
@Component({
  selector: 'app-proximas-reservas',
  imports: [DatePipe, SlicePipe],
  templateUrl: './proximas-reservas.html',
  styleUrl: './proximas-reservas.css',
})
export class ProximasReservas {
  /** Reservas a mostrar (máximo 4, ya filtradas y ordenadas por el Inicio). */
  readonly reservas = input.required<ReservaSalonModel[]>();

  /** true mientras llegan los datos. */
  readonly cargando = input.required<boolean>();

  /** Avisa que el usuario pulsó "Ver todas". */
  readonly verTodas = output<void>();
}
