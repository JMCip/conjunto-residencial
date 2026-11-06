import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  EstadoReserva,
  NuevaReservaSalon,
  ReservaSalonModel,
} from '../models/reserva-salon.model';

/**
 * Servicio que consulta y modifica las reservas del salón comunal a través de /api/reservas.
 * Todas las URL son relativas: el proxy de desarrollo las reenvía al backend.
 */
@Injectable({
  providedIn: 'root',
})
export class ReservaSalonService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso (/api/reservas). */
  private readonly urlbase = `${environment.apiUrl}/reservas`;

  /**
   * Obtiene todas las reservas con sus horas y su valor total.
   * @returns Observable con la lista completa.
   */
  getTodos(): Observable<ReservaSalonModel[]> {
    return this.cliente.get<ReservaSalonModel[]>(this.urlbase);
  }

  /**
   * Crea una reserva; queda CONFIRMADA. El backend revisa mora, sanciones pendientes y cruces de horario.
   * @param reserva Datos de la reserva.
   * @returns Observable con el mensaje y el id de la reserva creada.
   */
  crear(reserva: NuevaReservaSalon): Observable<{ mensaje: string; idReserva: number }> {
    return this.cliente.post<{ mensaje: string; idReserva: number }>(this.urlbase, reserva);
  }

  /**
   * Cambia el estado de una reserva CONFIRMADA.
   * @param id Identificador de la reserva.
   * @param estado Estado nuevo: CANCELADA, FINALIZADA o DEPOSITO_RETENIDO.
   * @returns Observable con el mensaje del backend.
   */
  actualizarEstado(id: number, estado: EstadoReserva): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(`${this.urlbase}/${id}`, { estado });
  }

  /**
   * Elimina una reserva CANCELADA.
   * @param id Identificador de la reserva.
   * @returns Observable con el mensaje del backend.
   */
  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(`${this.urlbase}/${id}`);
  }
}
