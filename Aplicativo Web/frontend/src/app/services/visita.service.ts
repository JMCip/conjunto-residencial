import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { VisitaModel } from '../models/visita.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

@Injectable({
  providedIn: 'root',
})
export class VisitaService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/visitas`;

  getTodos(): Observable<VisitaModel[]> {
    return this.cliente.get<VisitaModel[]>(this.urlbase);
  }

  postCrear(visita: Omit<VisitaModel, 'idVisita'>): Observable<RespuestaApiModel> {
    return this.cliente.post<RespuestaApiModel>(this.urlbase, visita);
  }

  putActualizar(visita: VisitaModel): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(`${this.urlbase}/${visita.idVisita}`, visita);
  }

  deleteEliminar(idVisita: number): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${idVisita}`);
  }
}
