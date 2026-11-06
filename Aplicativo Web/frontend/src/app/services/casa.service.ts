import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CasaModel } from '../models/casa.model';
import { RespuestaApiModel } from '../models/respuesta-api.model';

/**
 * Servicio que consulta y modifica las casas del conjunto a través de /api/casas.
 * Todas las URL son relativas: el proxy de desarrollo las reenvía al backend.
 */
@Injectable({
  providedIn: 'root',
})
export class CasaService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso (/api/casas). */
  private readonly urlbase = `${environment.apiUrl}/casas`;

  /**
   * Obtiene todas las casas.
   * @returns Observable con la lista completa.
   */
  getTodos(): Observable<CasaModel[]> {
    return this.cliente.get<CasaModel[]>(this.urlbase);
  }

  /**
   * Crea una casa.
   * @param casa Datos de la casa, incluida su llave (manzana y número).
   * @returns Observable con el mensaje del backend.
   */
  crear(casa: CasaModel): Observable<RespuestaApiModel> {
    return this.cliente.post<RespuestaApiModel>(this.urlbase, casa);
  }

  /**
   * Actualiza una casa. La llave (manzana y número) va en la URL y el cuerpo lleva el resto de datos.
   * @param casa Casa con los datos nuevos.
   * @returns Observable con el mensaje del backend.
   */
  actualizar(casa: CasaModel): Observable<RespuestaApiModel> {
    return this.cliente.put<RespuestaApiModel>(
      `${this.urlbase}/${casa.manzana}/${casa.numero}`,
      casa,
    );
  }

  /**
   * Elimina una casa.
   * @param manzana Manzana de la casa.
   * @param numero Número de la casa.
   * @returns Observable con el mensaje del backend.
   */
  eliminar(manzana: number, numero: number): Observable<RespuestaApiModel> {
    return this.cliente.delete<RespuestaApiModel>(`${this.urlbase}/${manzana}/${numero}`);
  }
}
