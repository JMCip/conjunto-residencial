import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  EstadoReserva,
  NuevaReservaSalon,
  ReservaSalonModel,
} from '../models/reserva-salon.model';

@Injectable({
  providedIn: 'root',
})
export class ReservaSalonService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/reservas`;

  getTodos(): Observable<ReservaSalonModel[]> {
    return this.cliente.get<ReservaSalonModel[]>(this.urlbase);
  }

  /** El back responde { mensaje, idReserva }. */
  crear(reserva: NuevaReservaSalon): Observable<{ mensaje: string; idReserva: number }> {
    return this.cliente.post<{ mensaje: string; idReserva: number }>(this.urlbase, reserva);
  }

  /** Solo cambia el estado: CANCELADA, FINALIZADA o DEPOSITO_RETENIDO. */
  actualizarEstado(id: number, estado: EstadoReserva): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(`${this.urlbase}/${id}`, { estado });
  }

  /** Solo reservas CANCELADAS. */
  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(`${this.urlbase}/${id}`);
  }
}
