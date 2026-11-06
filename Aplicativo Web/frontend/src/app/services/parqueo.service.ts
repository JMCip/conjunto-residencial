import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ParqueoModel } from '../models/parqueo.model';

/**
 * Servicio que consulta y modifica los registros de entrada y salida de vehículos a través de /api/parqueos.
 * Todas las URL son relativas: el proxy de desarrollo las reenvía al backend.
 */
@Injectable({
  providedIn: 'root',
})
export class ParqueoService {
  /** Cliente HTTP de Angular para llamar al backend. */
  private cliente: HttpClient = inject(HttpClient);

  /** URL base del recurso (/api/parqueos). */
  private readonly urlbase = `${environment.apiUrl}/parqueos`;

  /**
   * Obtiene todos los parqueos.
   * @returns Observable con la lista completa.
   */
  getTodos(): Observable<ParqueoModel[]> {
    return this.cliente.get<ParqueoModel[]>(this.urlbase);
  }

  /**
   * Obtiene un parqueo por su identificador.
   * @param id Valor de idParqueo.
   * @returns Observable con el registro.
   */
  getPorId(id: number): Observable<ParqueoModel> {
    return this.cliente.get<ParqueoModel>(`${this.urlbase}/${id}`);
  }

  /**
   * Crea un parqueo; el identificador lo genera la base de datos.
   * @param parqueo Datos del registro sin idParqueo.
   * @returns Observable con el mensaje del backend y el idParqueo creado.
   */
  crear(parqueo: Omit<ParqueoModel, 'idParqueo'>): Observable<{ mensaje: string; idParqueo: number }> {
    return this.cliente.post<{ mensaje: string; idParqueo: number }>(this.urlbase, parqueo);
  }

  /**
   * Actualiza un parqueo; el identificador va en la URL.
   * @param parqueo Registro con los datos nuevos.
   * @returns Observable con el mensaje del backend.
   */
  actualizar(parqueo: ParqueoModel): Observable<{ mensaje: string }> {
    return this.cliente.put<{ mensaje: string }>(`${this.urlbase}/${parqueo.idParqueo}`, parqueo);
  }

  /**
   * Elimina un parqueo.
   * @param id Valor de idParqueo.
   * @returns Observable con el mensaje del backend.
   */
  eliminar(id: number): Observable<{ mensaje: string }> {
    return this.cliente.delete<{ mensaje: string }>(`${this.urlbase}/${id}`);
  }

}
