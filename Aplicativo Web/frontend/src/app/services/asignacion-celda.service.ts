import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AsignacionCeldaModel } from '../models/asignacion-celda.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

@Injectable({
  providedIn: 'root',
})
export class AsignacionCeldaService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/asignaciones-celda`;

  getTodos(): Observable<AsignacionCeldaModel[]> {
    return this.cliente.get<AsignacionCeldaModel[]>(this.urlbase);
  }

  postCrear(asignacion: Omit<AsignacionCeldaModel, 'idAsignacion'>): Observable<RespuestaApiModel> {
    return this.cliente.post<RespuestaApiModel>(this.urlbase, asignacion);
  }

  putActualizar(asignacion: AsignacionCeldaModel): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(
      `${this.urlbase}/${asignacion.idAsignacion}`,
      asignacion,
    );
  }

  deleteEliminar(idAsignacion: number): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${idAsignacion}`);
  }
}
