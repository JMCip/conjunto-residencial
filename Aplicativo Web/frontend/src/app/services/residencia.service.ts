import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ResidenciaModel } from '../models/residencia.model';

/**
 * Servicio que consulta y modifica las residencias a través de /api/residencias.
 * Todas las URL son relativas: el proxy de desarrollo las reenvía al backend.
 */
@Injectable({
  providedIn: 'root',
})
export class ResidenciaService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso (/api/residencias). */
  private readonly urlbase = `${environment.apiUrl}/residencias`;

  /**
   * Obtiene todas las residencias.
   * @returns Observable con la lista completa.
   */
  getTodos(): Observable<ResidenciaModel[]> {
    return this.cliente.get<ResidenciaModel[]>(this.urlbase);
  }

  /**
   * Obtiene una residencia por su identificador.
   * @param id Valor de idResidencia.
   * @returns Observable con el registro.
   */
  getPorId(id: number): Observable<ResidenciaModel> {
    return this.cliente.get<ResidenciaModel>(`${this.urlbase}/${id}`);
  }

  /**
   * Crea una residencia; el identificador lo genera la base de datos.
   * @param residencia Datos del registro sin idResidencia.
   * @returns Observable con el mensaje del backend y el idResidencia creado.
   */
  crear(residencia: Omit<ResidenciaModel, 'idResidencia'>): Observable<{ mensaje: string; idResidencia: number }> {
    return this.cliente.post<{ mensaje: string; idResidencia: number }>(this.urlbase, residencia);
  }

  /**
   * Actualiza una residencia; el identificador va en la URL.
   * @param residencia Registro con los datos nuevos.
   * @returns Observable con el mensaje del backend.
   */
  actualizar(residencia: ResidenciaModel): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(`${this.urlbase}/${residencia.idResidencia}`, residencia);
  }

  /**
   * Elimina una residencia.
   * @param id Valor de idResidencia.
   * @returns Observable con el mensaje del backend.
   */
  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(`${this.urlbase}/${id}`);
  }
}

