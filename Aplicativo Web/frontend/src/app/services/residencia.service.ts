import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ResidenciaModel } from '../models/residencia.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

@Injectable({
  providedIn: 'root',
})
export class ResidenciaService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/residencias`;

  getTodos(): Observable<ResidenciaModel[]> {
    return this.cliente.get<ResidenciaModel[]>(this.urlbase);
  }

  postCrear(residencia: Omit<ResidenciaModel, 'idResidencia'>): Observable<RespuestaApiModel> {
    return this.cliente.post<RespuestaApiModel>(this.urlbase, residencia);
  }

  putActualizar(residencia: ResidenciaModel): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(
      `${this.urlbase}/${residencia.idResidencia}`,
      residencia,
    );
  }

  deleteEliminar(idResidencia: number): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${idResidencia}`);
  }
}
