import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ArriendoModel } from '../models/arriendo.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

@Injectable({
  providedIn: 'root',
})
export class ArriendoService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/arriendos`;

  getTodos(): Observable<ArriendoModel[]> {
    return this.cliente.get<ArriendoModel[]>(this.urlbase);
  }

  postCrear(arriendo: Omit<ArriendoModel, 'idContrato'>): Observable<RespuestaApiModel> {
    return this.cliente.post<RespuestaApiModel>(this.urlbase, arriendo);
  }

  putActualizar(arriendo: ArriendoModel): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(`${this.urlbase}/${arriendo.idContrato}`, arriendo);
  }

  deleteEliminar(idContrato: number): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${idContrato}`);
  }
}
