import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DatosReservaSalon, ReservaSalonModel } from '../models/reserva-salon.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

@Injectable({
  providedIn: 'root',
})
export class ReservaSalonService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/reservas`;

  getTodos(): Observable<ReservaSalonModel[]> {
    return this.cliente.get<ReservaSalonModel[]>(this.urlbase);
  }

  postCrear(reserva: DatosReservaSalon): Observable<RespuestaApiModel> {
    return this.cliente.post<RespuestaApiModel>(this.urlbase, reserva);
  }

  putActualizar(idReserva: number, reserva: DatosReservaSalon): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(`${this.urlbase}/${idReserva}`, reserva);
  }

  deleteEliminar(idReserva: number): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${idReserva}`);
  }
}
