import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CeldaParqueaderoModel } from '../models/celda-parqueadero.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

@Injectable({
  providedIn: 'root',
})
export class CeldaParqueaderoService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/celdas`;

  getTodos(): Observable<CeldaParqueaderoModel[]> {
    return this.cliente.get<CeldaParqueaderoModel[]>(this.urlbase);
  }

  postCrear(celda: Omit<CeldaParqueaderoModel, 'idCelda'>): Observable<RespuestaApiModel> {
    return this.cliente.post<RespuestaApiModel>(this.urlbase, celda);
  }

  putActualizar(celda: CeldaParqueaderoModel): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(`${this.urlbase}/${celda.idCelda}`, celda);
  }

  deleteEliminar(idCelda: number): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${idCelda}`);
  }
}
