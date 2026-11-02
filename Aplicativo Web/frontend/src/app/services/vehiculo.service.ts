import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { VehiculoModel } from '../models/vehiculo.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

@Injectable({
  providedIn: 'root',
})
export class VehiculoService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/vehiculos`;

  getTodos(): Observable<VehiculoModel[]> {
    return this.cliente.get<VehiculoModel[]>(this.urlbase);
  }

  postCrear(vehiculo: VehiculoModel): Observable<RespuestaApiModel> {
    return this.cliente.post<RespuestaApiModel>(this.urlbase, vehiculo);
  }

  /** La placa es la llave: la URL lleva la placa vieja y el cuerpo los datos nuevos. */
  putActualizar(placaAnterior: string, vehiculo: VehiculoModel): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(`${this.urlbase}/${placaAnterior}`, vehiculo);
  }

  deleteEliminar(placa: string): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${placa}`);
  }
}
