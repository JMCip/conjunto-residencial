import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ReservaSalonModel } from '../models/reserva-salon.model';

@Injectable({
  providedIn: 'root',
})
export class ReservaSalonService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/reservas`;

  getTodos(): Observable<ReservaSalonModel[]> {
    return this.cliente.get<ReservaSalonModel[]>(this.urlbase);
  }
}
