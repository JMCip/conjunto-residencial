import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { SancionModel } from '../models/sancion.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

@Injectable({
  providedIn: 'root',
})
export class SancionService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/sanciones`;

  getTodos(): Observable<SancionModel[]> {
    return this.cliente.get<SancionModel[]>(this.urlbase);
  }

  postCrear(sancion: Omit<SancionModel, 'idSancion'>): Observable<RespuestaApiModel> {
    return this.cliente.post<RespuestaApiModel>(this.urlbase, sancion);
  }

  putActualizar(sancion: SancionModel): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(`${this.urlbase}/${sancion.idSancion}`, sancion);
  }

  deleteEliminar(idSancion: number): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${idSancion}`);
  }
}
