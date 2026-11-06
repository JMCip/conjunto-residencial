import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PosesionModel } from '../models/posesion.model';

/**
 * Servicio que consulta y modifica las posesiones de casas a través de /api/posesiones.
 * Todas las URL son relativas: el proxy de desarrollo las reenvía al backend.
 */
@Injectable({
  providedIn: 'root',
})
export class PosesionService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso (/api/posesiones). */
  private readonly urlbase = `${environment.apiUrl}/posesiones`;

  /**
   * Obtiene todas las posesiones.
   * @returns Observable con la lista completa.
   */
  getTodos(): Observable<PosesionModel[]> {
    return this.cliente.get<PosesionModel[]>(this.urlbase);
  }

  /**
   * Obtiene una posesión por su identificador.
   * @param id Valor de idPosesion.
   * @returns Observable con el registro.
   */
  getPorId(id: number): Observable<PosesionModel> {
    return this.cliente.get<PosesionModel>(`${this.urlbase}/${id}`);
  }

  /**
   * Crea una posesión; el identificador lo genera la base de datos.
   * @param posesion Datos del registro sin idPosesion.
   * @returns Observable con el mensaje del backend y el idPosesion creado.
   */
  crear(posesion: Omit<PosesionModel, 'idPosesion'>): Observable<{ mensaje: string; idPosesion: number }> {
    return this.cliente.post<{ mensaje: string; idPosesion: number }>(this.urlbase, posesion);
  }

  /**
   * Actualiza una posesión; el identificador va en la URL.
   * @param posesion Registro con los datos nuevos.
   * @returns Observable con el mensaje del backend.
   */
  actualizar(posesion: PosesionModel): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(`${this.urlbase}/${posesion.idPosesion}`, posesion);
  }

  /**
   * Elimina una posesión.
   * @param id Valor de idPosesion.
   * @returns Observable con el mensaje del backend.
   */
  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(`${this.urlbase}/${id}`);
  }
}
