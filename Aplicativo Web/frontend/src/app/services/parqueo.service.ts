import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ParqueoModel } from '../models/parqueo.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

@Injectable({
  providedIn: 'root',
})
export class ParqueoService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/parqueos`;

  getTodos(): Observable<ParqueoModel[]> {
    return this.cliente.get<ParqueoModel[]>(this.urlbase);
  }

  postCrear(parqueo: Omit<ParqueoModel, 'idParqueo'>): Observable<RespuestaApiModel> {
    return this.cliente.post<RespuestaApiModel>(this.urlbase, parqueo);
  }

  putActualizar(parqueo: ParqueoModel): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(`${this.urlbase}/${parqueo.idParqueo}`, parqueo);
  }

  deleteEliminar(idParqueo: number): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${idParqueo}`);
  }
}
