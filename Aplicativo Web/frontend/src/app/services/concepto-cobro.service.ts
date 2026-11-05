import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ConceptoCobroModel, DatosConcepto } from '../models/concepto-cobro.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

@Injectable({
  providedIn: 'root',
})
export class ConceptoCobroService {
  private cliente: HttpClient = inject(HttpClient);

  private readonly urlbase = `${environment.apiUrl}/conceptos`;

  getTodos(): Observable<ConceptoCobroModel[]> {
    return this.cliente.get<ConceptoCobroModel[]>(this.urlbase);
  }

  /** El back responde { mensaje, idConcepto }; aquí solo se usa el mensaje. */
  crear(datos: DatosConcepto): Observable<RespuestaApiModel> {
    return this.cliente.post<RespuestaApiModel>(this.urlbase, datos);
  }

  /** El id va en la URL; el body lleva nombre, descripcion y valorBase. */
  actualizar(id: number, datos: DatosConcepto): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(`${this.urlbase}/${id}`, datos);
  }

  /** El back rechaza (409) los protegidos y los que ya se usaron en líneas de factura. */
  eliminar(id: number): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${id}`);
  }
}
