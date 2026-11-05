import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CasaModel } from '../models/casa.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

@Injectable({
  providedIn: 'root',
})
export class CasaService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/casas`;

  getTodos(): Observable<CasaModel[]> {
    return this.cliente.get<CasaModel[]>(this.urlbase);
  }

  crear(casa: CasaModel): Observable<RespuestaApiModel> {
    return this.cliente.post<RespuestaApiModel>(this.urlbase, casa);
  }

  /** La llave (manzana/numero) va en la URL; el body lleva area, estado y numeroHabitaciones. */
  actualizar(casa: CasaModel): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(
      `${this.urlbase}/${casa.manzana}/${casa.numero}`,
      casa,
    );
  }

  eliminar(manzana: number, numero: number): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${manzana}/${numero}`);
  }
}
