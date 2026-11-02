import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PosesionModel } from '../models/posesion.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

@Injectable({
  providedIn: 'root',
})
export class PosesionService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/posesiones`;

  getTodos(): Observable<PosesionModel[]> {
    return this.cliente.get<PosesionModel[]>(this.urlbase);
  }

  postCrear(posesion: Omit<PosesionModel, 'idPosesion'>): Observable<RespuestaApiModel> {
    return this.cliente.post<RespuestaApiModel>(this.urlbase, posesion);
  }

  putActualizar(posesion: PosesionModel): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(`${this.urlbase}/${posesion.idPosesion}`, posesion);
  }

  deleteEliminar(idPosesion: number): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${idPosesion}`);
  }
}
