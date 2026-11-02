import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TelefonoModel } from '../models/telefono.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

@Injectable({
  providedIn: 'root',
})
export class TelefonoService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/telefonos`;

  getTodos(): Observable<TelefonoModel[]> {
    return this.cliente.get<TelefonoModel[]>(this.urlbase);
  }

  postCrear(telefono: TelefonoModel): Observable<RespuestaApiModel> {
    return this.cliente.post<RespuestaApiModel>(this.urlbase, telefono);
  }

  /** El teléfono es la llave: la URL lleva el número viejo y el cuerpo los datos nuevos. */
  putActualizar(telefonoAnterior: string, telefono: TelefonoModel): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(`${this.urlbase}/${telefonoAnterior}`, telefono);
  }

  deleteEliminar(telefono: string): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${telefono}`);
  }
}
