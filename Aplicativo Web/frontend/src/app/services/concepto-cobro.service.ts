import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ConceptoCobroModel, DatosConcepto } from '../models/concepto-cobro.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

/**
 * Servicio que consulta y modifica el catálogo de conceptos de cobro a través de /api/conceptos.
 * Todas las URL son relativas: el proxy de desarrollo las reenvía al backend.
 */
@Injectable({
  providedIn: 'root',
})
export class ConceptoCobroService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso (/api/conceptos). */
  private readonly urlbase = `${environment.apiUrl}/conceptos`;

  /**
   * Obtiene todos los conceptos de cobro.
   * @returns Observable con la lista completa.
   */
  getTodos(): Observable<ConceptoCobroModel[]> {
    return this.cliente.get<ConceptoCobroModel[]>(this.urlbase);
  }

  /**
   * Crea un concepto de cobro.
   * @param datos Nombre, descripción y valor base.
   * @returns Observable con el mensaje del backend.
   */
  crear(datos: DatosConcepto): Observable<RespuestaApiModel> {
    return this.cliente.post<RespuestaApiModel>(this.urlbase, datos);
  }

  /**
   * Actualiza un concepto de cobro; el identificador va en la URL.
   * @param id Identificador del concepto.
   * @param datos Nombre, descripción y valor base nuevos.
   * @returns Observable con el mensaje del backend.
   */
  actualizar(id: number, datos: DatosConcepto): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(`${this.urlbase}/${id}`, datos);
  }

  /**
   * Elimina un concepto de cobro. El backend responde 409 si el concepto es protegido o ya se usó en líneas de factura.
   * @param id Identificador del concepto.
   * @returns Observable con el mensaje del backend.
   */
  eliminar(id: number): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${id}`);
  }
}
